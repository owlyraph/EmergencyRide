package com.example.emergencyride.recyclerViewFollow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emergencyride.R;
import com.example.emergencyride.UserInformation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RCAdapter extends RecyclerView.Adapter<RCViewHolders> {

    private List<UsersObject> usersList;
    private Context context;

    public RCAdapter(List<UsersObject> usersList, Context context){

        this.usersList=usersList;
        this.context=context;
    }

    @NonNull
    @Override
    public RCViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView= LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_followers_item, null);
        RCViewHolders rcv=new RCViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull  final RCViewHolders holder, int position) {
        holder.mEmail.setText(usersList.get(position).getEmail());

        if (UserInformation.listFollowing.contains(usersList.get(holder.getLayoutPosition()).getUId())){
            holder.mFollow.setText("following");
        }else{
            holder.mFollow.setText("follow");
        }

        holder.mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String clientId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                String ambulanceId= FirebaseAuth.getInstance().getCurrentUser().getUid();

                if (UserInformation.listFollowing.contains(usersList.get(holder.getLayoutPosition()).getUId())){
                    holder.mFollow.setText("following");
                    FirebaseDatabase.getInstance().getReference().child("Users").child("AmbulaceDrivers").child(ambulanceId).child("following").child(usersList.get(holder.getLayoutPosition()).getUId()).setValue(true);
                }else{
                    holder.mFollow.setText("follow");
                    FirebaseDatabase.getInstance().getReference().child("Users").child("AmbulanceDrivers").child(ambulanceId).child("following").child(usersList.get(holder.getLayoutPosition()).getUId()).removeValue();

                }

                if (UserInformation.listFollowing.contains(usersList.get(holder.getLayoutPosition()).getUId())){
                    holder.mFollow.setText("following");
                    FirebaseDatabase.getInstance().getReference().child("Users").child("Clients").child(clientId).child("following").child(usersList.get(holder.getLayoutPosition()).getUId()).setValue(true);
                }else{
                    holder.mFollow.setText("follow");
                    FirebaseDatabase.getInstance().getReference().child("Users").child("Clients").child(clientId).child("following").child(usersList.get(holder.getLayoutPosition()).getUId()).removeValue();

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.usersList.size();
    }
}
