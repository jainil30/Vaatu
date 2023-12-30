package com.jainil.vaatu;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendViewHolder extends RecyclerView.ViewHolder {
    CircleImageView circleImageView;
    TextView username, profession;
    public FindFriendViewHolder(@NonNull View itemView) {
        super(itemView);

        circleImageView = itemView.findViewById(R.id.findFriendprofileImagecircleImageView);
        username = itemView.findViewById(R.id.findFrindUsernameTv);
        profession = itemView.findViewById(R.id.findFriendsProfessionTv);

    }
}
