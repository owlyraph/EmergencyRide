package com.example.emergencyride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.emergencyride.fragment.CameraFragment;
import com.example.emergencyride.fragment.ChatFragment;
import com.example.emergencyride.fragment.StoryFragment;
import com.example.emergencyride.historyRecyclerView.HistoryObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatActivity extends AppCompatActivity {
    private String clientChatOrAmbulanceChat, userId;

    FragmentPagerAdapter adapterViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        clientChatOrAmbulanceChat=getIntent().getExtras().getString("clientChatOrAmbulanceChat");
        userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserChatIds();


        ViewPager viewPager=findViewById(R.id.viewPager);
        adapterViewPager= new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);
        viewPager.setCurrentItem(1);
    }

    private void getUserChatIds() {
        DatabaseReference userChatDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(clientChatOrAmbulanceChat).child(userId).child("chat");
        userChatDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot chat : snapshot.getChildren()){
                        FetchChatInformation(chat.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void FetchChatInformation(String chatKey) {
        DatabaseReference chatDatabase= FirebaseDatabase.getInstance().getReference().child("chat").child(chatKey);
        chatDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static class MyPagerAdapter extends FragmentPagerAdapter{

        public MyPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return ChatFragment.newInstance();
                case 1:
                    return CameraFragment.newInstance();
                case 2:
                    return StoryFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}