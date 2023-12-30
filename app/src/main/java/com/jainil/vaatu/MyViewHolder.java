package com.jainil.vaatu;

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyViewHolder extends RecyclerView.ViewHolder {
    CircleImageView profileImage;

    ImageView postImage, likesImageView, commentsImageView, commentsSend;

    TextView username, timeAgo, postDescription, likesCount, commentsCount ;
    EditText inputComments;

    public static RecyclerView recyclerView;


    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        profileImage = itemView.findViewById(R.id.singleViewPostProfileCircleImageView);

        postImage = itemView.findViewById(R.id.singleViewPostImageView);
        username = itemView.findViewById(R.id.SingleViewPostUsernameTv);
        timeAgo = itemView.findViewById(R.id.SingleViewPostTimeTv);
        postDescription = itemView.findViewById(R.id.SingleViewPostDescriptionTv);
        likesCount = itemView.findViewById(R.id.SingleViewPostLikeCountTv);
        commentsCount = itemView.findViewById(R.id.SingleViewPostCommentCountTv);
        likesImageView = itemView.findViewById(R.id.singleViewPostLikesBtn);
        commentsImageView = itemView.findViewById(R.id.singleViewPostCommentBtn);
        inputComments = itemView.findViewById(R.id.singleViewPostCommentInputEt);
        commentsSend = itemView.findViewById(R.id.singleViewPostCommentSendBtn);
        recyclerView = itemView.findViewById(R.id.singleViewPostCommentRecyclerView);




    }


    public void countLike(String postKey, String uid, DatabaseReference likeReference) {
        likeReference.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int totalLikes = (int) snapshot.getChildrenCount();
                    likesCount.setText(totalLikes + " likes");
                }
                else{
                    likesCount.setText(0 + " likes");
                }

                if(snapshot.child(uid).exists()){
                    likesImageView.setColorFilter(Color.BLUE);
                }else{
                    likesImageView.setColorFilter(Color.GRAY);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void countComment(String postKey, String uid, DatabaseReference commentReference) {
        commentReference.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int totalComments = (int) snapshot.getChildrenCount();
                    commentsCount.setText(totalComments + " comments");
                }
                else{
                    commentsCount.setText(0 + " likes");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
