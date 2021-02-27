package com.example.emergencyride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.example.emergencyride.fragment.recyclerViewReceiver.ReceiverAdapter;
import com.example.emergencyride.fragment.recyclerViewReceiver.ReceiverObject;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChooseRecieverActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    String Uid;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_reciever);

        try {
            bitmap= BitmapFactory.decodeStream(getApplicationContext().openFileInput("ImageToSend"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            finish();
            return;
        }

        Uid= FirebaseAuth.getInstance().getUid();

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getApplication());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ReceiverAdapter(getDataSet(),getApplication());
        mRecyclerView.setAdapter(mAdapter);

        FloatingActionButton mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToStories();

            }
        });

    }

    private ArrayList<ReceiverObject> results=new ArrayList<>();
    private ArrayList<ReceiverObject> getDataSet() {
        listenForData();
        return results;
    }

    private void listenForData() {
        for (int i = 0; i < UserInformation.listFollowing.size(); i++) {
            DatabaseReference clientDb = FirebaseDatabase.getInstance().getReference().child("Users").child("ambulancesWorking").child(UserInformation.listFollowing.get(i));
            clientDb.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String email = "";
                    String uid = snapshot.getRef().getKey();
                    if (snapshot.child("email").getValue() != null) {
                        email = snapshot.child("email").getValue().toString();
                    }
                    ReceiverObject obj = new ReceiverObject(email, uid, false);
                    if (!results.contains(obj)) {
                        results.add(obj);
                        mAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    private void saveToStories() {
        final DatabaseReference clientStoryDb= FirebaseDatabase.getInstance().getReference().child("Users").child("clientRequest").child(Uid).child("story");
        final String key=clientStoryDb.push().getKey();

        final StorageReference filepath= FirebaseStorage.getInstance().getReference().child("captures").child(key);

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,20, baos);
        byte[] dataToUpload=baos.toByteArray();
        UploadTask uploadTask=filepath.putBytes(dataToUpload);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> imageUrl = taskSnapshot.getStorage().getDownloadUrl();
                        while (!imageUrl.isComplete()) ;
                        Uri url = imageUrl.getResult();
                        long currentTimeStamp = System.currentTimeMillis();
                        Long endTimeStamp = currentTimeStamp + (24 * 60 * 60 * 1000);

                        CheckBox mStory = findViewById(R.id.story);
                        if(mStory.isChecked()) {
                            Map<String, Object> mapToUpload = new HashMap<>();
                            mapToUpload.put("imageUrl", url.toString());
                            mapToUpload.put("timeStampBeg", currentTimeStamp);
                            mapToUpload.put("timeStampEnd", endTimeStamp);
                            clientStoryDb.child(key).setValue(mapToUpload);
                        }
                        for(int i = 0; i< results.size(); i++){
                            if(results.get(i).getReceive()){
                                DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Users").child("clientRequest").child(results.get(i).getUId()).child("received").child(Uid);
                                Map<String, Object> mapToUpload = new HashMap<>();
                                mapToUpload.put("imageUrl", url.toString());
                                mapToUpload.put("timeStampBeg", currentTimeStamp);
                                mapToUpload.put("timeStampEnd", endTimeStamp);
                                userDb.child(key).setValue(mapToUpload);
                            }
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        finish();
                        return;
                    }
                }
            }
        });



        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                finish();
                return;

            }
        });
    }
}