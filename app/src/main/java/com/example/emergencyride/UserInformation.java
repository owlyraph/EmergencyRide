package com.example.emergencyride;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class UserInformation {

    public static ArrayList<String> listFollowing=new ArrayList<>();

    public void startFetching(){
        listFollowing.clear();
        getUserFollowing();
    }

    private void getUserFollowing() {
        DatabaseReference ambulanceFollowingDB= FirebaseDatabase.getInstance().getReference().child("Users").child("ambulancesWorking").child(FirebaseAuth.getInstance().getUid()).child("following");
        ambulanceFollowingDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()){
                    String uid=snapshot.getRef().getKey();
                    if (uid !=null && ! listFollowing.contains(uid)){
                        listFollowing.add(uid);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    String uid=snapshot.getRef().getKey();
                    if (uid !=null){
                        listFollowing.remove(uid);
                    }
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference clientFollowingDB= FirebaseDatabase.getInstance().getReference().child("Users").child("clientRequest").child(FirebaseAuth.getInstance().getUid()).child("following");
        clientFollowingDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()){
                    String uid=snapshot.getRef().getKey();
                    if (uid !=null && ! listFollowing.contains(uid)){
                        listFollowing.add(uid);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String uid=snapshot.getRef().getKey();
                    if (uid !=null){
                        listFollowing.remove(uid);
                    }
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
