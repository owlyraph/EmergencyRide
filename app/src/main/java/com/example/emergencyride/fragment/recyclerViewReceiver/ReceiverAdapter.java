package com.example.emergencyride.fragment.recyclerViewReceiver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emergencyride.R;

import java.util.List;

public class ReceiverAdapter extends RecyclerView.Adapter<ReceiverViewHolders> {

    private List<ReceiverObject> usersList;
    private Context context;

    public ReceiverAdapter(List<ReceiverObject> usersList, Context context){

        this.usersList=usersList;
        this.context=context;
    }

    @NonNull
    @Override
    public ReceiverViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView= LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_receiver_item, null);
        ReceiverViewHolders rcv=new ReceiverViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull  final ReceiverViewHolders holder, int position) {
        holder.mEmail.setText(usersList.get(position).getEmail());
        holder.mReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean receiveState = !usersList.get(holder.getLayoutPosition()).getReceive();
                usersList.get(holder.getLayoutPosition()).setReceive(receiveState);
            }
        });


    }

    @Override
    public int getItemCount() {
        return this.usersList.size();
    }
}
