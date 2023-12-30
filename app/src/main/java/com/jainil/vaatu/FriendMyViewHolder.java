package com.jainil.vaatu;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendMyViewHolder extends RecyclerView.ViewHolder {

    CircleImageView profileImageCircleImageView;
    TextView usernameTv,professionTv;
    public FriendMyViewHolder(@NonNull View itemView) {
        super(itemView);

        profileImageCircleImageView = itemView.findViewById(R.id.friendprofileImagecircleImageView);
        usernameTv = itemView.findViewById(R.id.friendUsernameTv);
        professionTv = itemView.findViewById(R.id.friendsProfessionTv);

    }
}
