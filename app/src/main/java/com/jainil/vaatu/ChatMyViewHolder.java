package com.jainil.vaatu;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatMyViewHolder extends RecyclerView.ViewHolder {

    CircleImageView firstProfileCircleImageView, secondProfileCircleImageView;
    TextView firstUserMessageTv, secondUserMessageTv;

    public ChatMyViewHolder(@NonNull View itemView) {
        super(itemView);

        firstProfileCircleImageView = itemView.findViewById(R.id.firstUserProfileSingleViewMessage);
        secondProfileCircleImageView = itemView.findViewById(R.id.secondUserProfileSingleViewMessage);

        firstUserMessageTv = itemView.findViewById(R.id.firstUserTextSingleViewMessage);
        secondUserMessageTv = itemView.findViewById(R.id.secondUserTextSingleViewMessage);

    }
}
