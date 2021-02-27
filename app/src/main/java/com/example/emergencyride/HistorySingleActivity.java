package com.example.emergencyride;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity  implements OnMapReadyCallback, RoutingListener {

    private String helpId, currentUserId, clientId, ambulanceId, userAmbulanceOrClient;

    private TextView locationHelp;
    private TextView distanceHelp;
    private TextView dateHelp;
    private TextView nameUser;
    private TextView phoneUser;

    private ImageView imageUser;

    private RatingBar mRatingBar;

    private Button mPay;

    private DatabaseReference historyHelpInfoDb;
    private LatLng destinationLatLong, pickupLatLong;

    private String distance;
    private Double ridePrice;
    private Boolean clientPaid = false;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        polylines=new ArrayList<>();

        helpId=getIntent().getExtras().getString("helpId");

        mMapFragment=(SupportMapFragment) getSupportFragmentManager() .findFragmentById(R.id.map);

        mMapFragment.getMapAsync(this);

        locationHelp=(TextView) findViewById(R.id.helpLocation);
        distanceHelp=(TextView) findViewById(R.id.helpDistance);
        dateHelp=(TextView) findViewById(R.id.helpDate);
        nameUser=(TextView) findViewById(R.id.userName);
        phoneUser=(TextView) findViewById(R.id.phone);

        imageUser=(ImageView) findViewById(R.id.userImage);

        mRatingBar=(RatingBar) findViewById(R.id.ratingBar);

        mPay = findViewById(R.id.pay);

        currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();

        historyHelpInfoDb= FirebaseDatabase.getInstance().getReference().child("history").child(helpId);
        getHelpInfo();


    }

    private void getHelpInfo() {
        historyHelpInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot child:snapshot.getChildren()){
                        if (child.getKey().equals("client")){
                            clientId=child.getValue().toString();
                            if (!clientId.equals(currentUserId)){
                                userAmbulanceOrClient="Ambulances";
                                getUserInformation("Clients",clientId);
                            }
                        }
                        if (child.getKey().equals("ambulance")){
                            ambulanceId=child.getValue().toString();
                            if (!ambulanceId.equals(currentUserId)){
                                userAmbulanceOrClient="Clients";
                                getUserInformation("Ambulances",ambulanceId);
                                displayClientRelatedObjects();
                            }
                        }
                        if (child.getKey().equals("rating")){
                            mRatingBar.setRating(Integer.parseInt(child.getValue().toString()));
                        }
                        if (child.getKey().equals("clientPaid")){
                            clientPaid =true;
                        }
                        if (child.getKey().equals("distance")){
                            distance= child.getValue().toString();
                            distanceHelp.setText(distance.substring(0, Math.min(distance.length(),5)) + "km");
                            ridePrice= Double.valueOf(distance) * 0.5;
                        }
                        if (child.getKey().equals("timestamp")){
                            dateHelp.setText(getDate(Long.valueOf(child.getValue().toString())));

                        }
                        if (child.getKey().equals("destination")){
                            locationHelp.setText(getDate(Long.valueOf(child.getValue().toString())));

                        }
                        if (child.getKey().equals("location")){
                            pickupLatLong= new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()), Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            destinationLatLong= new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()), Double.valueOf(child.child("to").child("lng").getValue().toString()));

                            if (destinationLatLong !=new LatLng(0,0)){
                                getRouteToMarker();
                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayClientRelatedObjects() {
        mRatingBar.setVisibility(View.VISIBLE);
        mPay.setVisibility(View.VISIBLE);
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                historyHelpInfoDb.child("rating").setValue(rating);
                DatabaseReference mAmbulanceRatingDb=FirebaseDatabase.getInstance().getReference().child("Users").child("Ambulances").child(ambulanceId).child("rating");
                mAmbulanceRatingDb.child(helpId).setValue(rating);
            }
        });

        if(clientPaid){
            mPay.setEnabled(false);
        }else{
            mPay.setEnabled(true);
        }

        mPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payPalPayment();
            }
        });
    }

    private int PAYPAL_REQUEST_CODE = 1;
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID);

    private void payPalPayment() {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(ridePrice), "KES", "Service charge",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirm != null){
                    try{
                        JSONObject jsonObj = new JSONObject(confirm.toJSONObject().toString());

                        String paymentResponse = jsonObj.getJSONObject("response").getString("state");

                        if(paymentResponse.equals("approved")){
                            Toast.makeText(getApplicationContext(), "Payment successful", Toast.LENGTH_LONG).show();
                            historyHelpInfoDb.child("clientPaid").setValue(true);
                            mPay.setEnabled(false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }else{
                Toast.makeText(getApplicationContext(), "Payment unsuccessful", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    private void getUserInformation(String otherUserClientOrAmbulance, String otherUserId) {
        DatabaseReference mOtherUserDB=FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserClientOrAmbulance).child(otherUserId);
        mOtherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Map<String,Object> map=(Map<String,Object>) snapshot.getValue();
                    if (map.get("name")!=null){
                        nameUser.setText(map.get("name").toString());
                    }
                    if (map.get("phone")!=null){
                        phoneUser.setText(map.get("phone").toString());
                    }
                    if (map.get("profileImageUrl")!=null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(imageUser);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getDate(Long timestamp) {
        Calendar cal=Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);
        String date= DateFormat.format("dd-MM-yyyy hh:mm", cal).toString();
        return date;
    }
    private void getRouteToMarker() {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickupLatLong, destinationLatLong)
                .build();
        routing.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        LatLngBounds.Builder builder=new LatLngBounds.Builder();
        builder.include(pickupLatLong);
        builder.include(destinationLatLong);
        LatLngBounds bounds=builder.build();

        int width=getResources().getDisplayMetrics().widthPixels;
        int padding=(int) (width*0.2);

        CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cameraUpdate);

        mMap.addMarker(new MarkerOptions().position(pickupLatLong).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
        mMap.addMarker(new MarkerOptions().position(destinationLatLong).title("destination"));

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolylines(){
        for (Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }
}