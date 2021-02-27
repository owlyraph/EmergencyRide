package com.example.emergencyride.recyclerViewFollow;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emergencyride.R;

public class RCViewHolders extends RecyclerView.ViewHolder {
    public TextView mEmail;
    public Button mFollow;

    public RCViewHolders(@NonNull View itemView) {
        super(itemView);

        mEmail=itemView.findViewById(R.id.email);
        mFollow=itemView.findViewById(R.id.follow);
    }
}
