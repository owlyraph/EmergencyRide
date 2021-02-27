package com.example.emergencyride;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.emergencyride.recyclerViewFollow.RCAdapter;
import com.example.emergencyride.recyclerViewFollow.UsersObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class FindAmbulanceActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    EditText mInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_ambulance);

        mInput = findViewById(R.id.input);
        Button mSearch = findViewById(R.id.search);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getApplication());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RCAdapter(getDataSet(),getApplication());
        mRecyclerView.setAdapter(mAdapter);

        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear();
                listenForData();
            }
        });

    }

    private void clear() {
        int size = this.results.size();
        this.results.clear();
        mAdapter.notifyItemRangeChanged(0, size);
    }

    private void listenForData() {
        DatabaseReference clientDb = FirebaseDatabase.getInstance().getReference().child("Users").child("clientRequest");
        DatabaseReference AmbulanceDb = FirebaseDatabase.getInstance().getReference().child("Users").child("ambulancesWorking");
        Query query = clientDb.orderByChild("email").startAt(mInput.getText().toString()).endAt(mInput.getText().toString() + "\uf8ff");
        Query query1 = AmbulanceDb.orderByChild("email").startAt(mInput.getText().toString()).endAt(mInput.getText().toString() + "\uf8ff");

        query1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String email = "";
                String uid = snapshot.getRef().getKey();
                if(snapshot.child("email").getValue() != null){
                    email = snapshot.child("email").getValue().toString();
                }
                if(!email.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                    UsersObject obj=new UsersObject(email, uid);
                    results.add(obj);
                    mAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String email = "";
                String uid = snapshot.getRef().getKey();
                if(snapshot.child("email").getValue() != null){
                    email = snapshot.child("email").getValue().toString();
                }
                if(!email.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                    UsersObject obj=new UsersObject(email, uid);
                    results.add(obj);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private ArrayList<UsersObject> results=new ArrayList<>();
    private ArrayList<UsersObject> getDataSet() {
        listenForData();
        return results;
    }
}