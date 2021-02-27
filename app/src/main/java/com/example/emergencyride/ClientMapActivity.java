package com.example.emergencyride;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;


    private FusedLocationProviderClient mFusedLocationClient;

    private Button mLogout, mRequest, mSettings, mHistory, mChat;
    private LatLng pickupLocation;
    private Boolean requestBol=false;
    private Marker pickupMarker;
    private SupportMapFragment mapFragment;
    private String destination,requestService;
    private LatLng destinationLatLong;
    private LinearLayout mDriverInfo;
    private ImageView mDriverProfileImage;
    private TextView mDriverName, mDriverPhone, mAmbulance;
    private RadioGroup mRadioGroup;
    private RatingBar mRatingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_map);

        UserInformation userInformation=new UserInformation();
        userInformation.startFetching();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mFusedLocationClient= LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        destinationLatLong=new LatLng(0.0, 0.0);

        mDriverInfo=(LinearLayout) findViewById(R.id.driverInfo);
        mDriverProfileImage=(ImageView) findViewById(R.id.driverProfileImage);
        mDriverName=(TextView) findViewById(R.id.driverName);
        mDriverPhone=(TextView) findViewById(R.id.driverPhone);
        mAmbulance=(TextView) findViewById(R.id.ambulance);

        mRatingBar=(RatingBar) findViewById(R.id.ratingBar);

        mRadioGroup=(RadioGroup) findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.St_John);
        mLogout=(Button) findViewById(R.id.Logout);
        mRequest=(Button) findViewById(R.id.request);
        mSettings=(Button) findViewById(R.id.settings);
        mHistory=(Button) findViewById(R.id.history);
        mChat=(Button) findViewById(R.id.chat);
        mChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ClientMapActivity.this,ChatActivity.class);
                intent.putExtra("clientChatOrAmbulanceChat","Clients");
                startActivity(intent);
                return;
            }
        });
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(ClientMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestBol){
                    endHelp();

                }else{
                    int selectId=mRadioGroup.getCheckedRadioButtonId();

                    final RadioButton radioButton=(RadioButton) findViewById(selectId);
                    if (radioButton.getText() == null){
                        return;
                    }

                    requestService=radioButton.getText().toString();

                    requestBol=true;
                    String userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("clientRequest");
                    GeoFire geoFire= new GeoFire(ref);
                    geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                    pickupLocation= new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                    pickupMarker=mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here"));;

                    mRequest.setText("Getting you Help....");

                    getClosestAmbulance();
                }

            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ClientMapActivity.this, ClientSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });
        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ClientMapActivity.this, HistoryActivity.class);
                intent.putExtra("clientOrAmbulance","Clients");
                startActivity(intent);
                return;
            }
        });

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyADdIXVJpL50hC77j3BzSL3kT_xYIP4BzQ");
        }


// Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(destination, "Place: " + place.getName() + ", " + place.getId());
                Log.i(String.valueOf(destinationLatLong), "LatLong " + place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });
    }
    private int radius=1;
    private Boolean ambulanceFound=false;
    private String ambulanceFoundID;
    GeoQuery geoQuery;
    private void getClosestAmbulance(){
        DatabaseReference ambulanceLocation=FirebaseDatabase.getInstance().getReference().child("ambulancesAvailable");

        GeoFire geoFire= new GeoFire(ambulanceLocation);
        geoQuery=geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!ambulanceFound && requestBol){
                    DatabaseReference mClientDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child("AmbulanceDrivers").child(key);
                    mClientDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.getChildrenCount()>0){
                                Map<String, Object> ambulanceMap=(Map<String, Object>) snapshot.getValue();

                                if (ambulanceFound){
                                    return;
                                }
                                if (ambulanceMap.get("service").equals(requestService)){
                                    ambulanceFound=true;
                                    ambulanceFoundID= snapshot.getKey();
                                    DatabaseReference ambulanceRef=FirebaseDatabase.getInstance().getReference().child("Users").child("AmbulanceDrivers").child(ambulanceFoundID).child("clientRequest");
                                    String clientId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map=new HashMap();
                                    map.put("clientHelpId",clientId);
                                    map.put("destination",destination);
                                    map.put("destinationLat",destinationLatLong.latitude);
                                    map.put("destinationLong",destinationLatLong.longitude);
                                    ambulanceRef.updateChildren(map);

                                    getAmbulanceLocation();
                                    getAmbulanceInfo();
                                    getHasHelpEnded();
                                    mRequest.setText("Looking for Ambulance Location....");

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }


            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!ambulanceFound){
                    radius++;
                    getClosestAmbulance();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }
    private Marker mAmbulanceMarker;
    private DatabaseReference ambulanceLocationRef;
    private ValueEventListener ambulanceLocationRefListener;
    private void getAmbulanceLocation(){
        ambulanceLocationRef=FirebaseDatabase.getInstance().getReference().child("ambulanceWorking").child(ambulanceFoundID).child("l");
        ambulanceLocationRefListener= ambulanceLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()&& requestBol){
                    List<Object> map=(List<Object>) snapshot.getValue();
                    double locationLat=0;
                    double locationLong=0;
                    mRequest.setText("Ambulance Found!");
                    if (map.get(0) !=null){
                        locationLat=Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) !=null){
                        locationLong=Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLong=new LatLng(locationLat,locationLong);
                    if (mAmbulanceMarker !=null){
                        mAmbulanceMarker.remove();
                    }
                    Location loc1= new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2= new Location("");
                    loc2.setLatitude(driverLatLong.latitude);
                    loc2.setLongitude(driverLatLong.longitude);

                    float distance=loc1.distanceTo(loc2);

                    if (distance<100){
                        mRequest.setText("Ambulance Is Here!");
                    }else{
                        mRequest.setText("Ambulance Is Here!"+String.valueOf(distance));
                    }



                    mAmbulanceMarker= mMap.addMarker(new MarkerOptions().position(driverLatLong).title("your ambulance").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void getAmbulanceInfo(){
        mDriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mClientDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("AmbulanceDrivers").child(ambulanceFoundID);
        mClientDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount()>0){

                    if(snapshot.child("name")!=null){
                        mDriverName.setText(snapshot.child("name").getValue().toString());
                    }
                    if(snapshot.child("phone")!=null){
                        mDriverPhone.setText(snapshot.child("phone").getValue().toString());
                    }
                    if(snapshot.child("ambulance")!=null){
                        mAmbulance.setText(snapshot.child("ambulance").getValue().toString());
                    }
                    if(snapshot.child("profileImageUrl").getValue()!=null){
                        Glide.with(getApplication()).load(snapshot.child("profileImageUrl").getValue().toString()).into(mDriverProfileImage);
                    }
                    int ratingSum=0;
                    float ratingTotal=0;
                    float ratingAvg=0;
                    for (DataSnapshot child: snapshot.child("rating").getChildren()){
                        ratingSum= ratingSum +Integer.valueOf(child.getValue().toString());
                        ratingTotal++;
                    }
                    if (ratingTotal!=0){
                        ratingAvg= ratingSum/ratingTotal;
                        mRatingBar.setRating(ratingAvg);
                    }


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private DatabaseReference helpHasEndedRef;
    private ValueEventListener helpHasEndedRefListener;
    private void getHasHelpEnded(){
        helpHasEndedRef= FirebaseDatabase.getInstance().getReference().child("Users").child("AmbulanceDrivers").child(ambulanceFoundID).child("clientRequest").child("clientHelpId");
        helpHasEndedRefListener= helpHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                }else {
                    endHelp();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void endHelp() {
        requestBol = false;
        geoQuery.removeAllListeners();
        ambulanceLocationRef.removeEventListener(ambulanceLocationRefListener);
        helpHasEndedRef.removeEventListener(helpHasEndedRefListener);

        if (ambulanceFoundID != null) {
            DatabaseReference ambulanceRef = FirebaseDatabase.getInstance().getReference().child("Users").child("AmbulanceDrivers").child(ambulanceFoundID).child("clientRequest");
            ambulanceRef.removeValue();
            ambulanceFoundID = null;
        }
        ambulanceFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("clientRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        if (pickupMarker != null) {
            pickupMarker.remove();
        }if (mAmbulanceMarker !=null){
            mAmbulanceMarker.remove();
        }
        mRequest.setText("Call Ambulance");

        mDriverInfo.setVisibility(View.GONE);
        mDriverName.setText("");
        mDriverPhone.setText("");
        mAmbulance.setText("");
        mDriverProfileImage.setImageResource(R.mipmap.default_user);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationRequest= new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else {
                checkLocationPermission();
            }
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);

    }
    LocationCallback mLocationCallback=new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                    if(!getAmbulancesAroundStarted)
                        getAmbulancesAround();
                }
            }
        }
    };

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(ClientMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1 );
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(ClientMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1 );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
    boolean getAmbulancesAroundStarted = false;
    List<Marker> markers = new ArrayList<Marker>();
    private void getAmbulancesAround() {
        getAmbulancesAroundStarted = true;
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLongitude(), mLastLocation.getLatitude()), 999999999);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                for (Marker markerIt : markers) {
                    if (markerIt.getTag().equals(key))
                        return;
                }
                LatLng ambulanceLocation = new LatLng(location.latitude, location.longitude);

                Marker mDriverMarker = mMap.addMarker(new MarkerOptions().position(ambulanceLocation).title(key).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
                mDriverMarker.setTag(key);

                markers.add(mDriverMarker);

            }

            @Override
            public void onKeyExited(String key) {
                for (Marker markerIt : markers) {
                    if (markerIt.getTag().equals(key)) {
                        markerIt.remove();
                    }
                }
            }


            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker markerIt : markers) {
                    if (markerIt.getTag().equals(key)) {
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }


            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

}