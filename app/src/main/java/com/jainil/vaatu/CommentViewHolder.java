package com.jainil.vaatu;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    CircleImageView profileImageView;
    TextView username, comment;
    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);

        profileImageView = itemView.findViewById(R.id.singleViewCommentProfileCircleImageView);
        username = itemView.findViewById(R.id.singleViewCommentUsernameTv);
        comment = itemView.findViewById(R.id.singleViewCommentCommentTv);


    }
}
