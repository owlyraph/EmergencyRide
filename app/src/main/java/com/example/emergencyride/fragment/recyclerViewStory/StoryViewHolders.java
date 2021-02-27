package com.example.emergencyride.fragment.recyclerViewStory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emergencyride.DisplayImageActivity;
import com.example.emergencyride.R;

public class StoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView mEmail;
    public LinearLayout mLayout;

    public StoryViewHolders(@NonNull View itemView) {
        super(itemView);

        mEmail=itemView.findViewById(R.id.email);
        mLayout=itemView.findViewById(R.id.layout);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), DisplayImageActivity.class);
        Bundle b = new Bundle();
        b.putString("userId", mEmail.getTag().toString());
        b.putString("chatOrStory", mLayout.getTag().toString());
        intent.putExtras(b);
        v.getContext().startActivity(intent);
    }
}

