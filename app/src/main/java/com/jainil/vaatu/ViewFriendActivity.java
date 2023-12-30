package com.jainil.vaatu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewFriendActivity extends AppCompatActivity {

    CircleImageView profileImage;
    TextView usernameTv, addressTv;
    String profileImageUrl, username, city, country, profession;
    String myProfileImageUrl, myUsername, myCity, myCountry, myProfession;
    String currentState = "nothing_happened";
    AppCompatButton btnPerform, btnDecline;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference userReference, requestReference, friendReference;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friend);

        userId = getIntent().getStringExtra("userKey");

        profileImage = findViewById(R.id.ViewFriendProfilecircleImageView);
        usernameTv = findViewById(R.id.ViewFriendUsernameTv);
        addressTv = findViewById(R.id.ViewFriendAddressTv);
        btnPerform = findViewById(R.id.viewFriendSendFriendRequestBtn);
        btnDecline = findViewById(R.id.viewFriendDeclineFriendRequestBtn);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        requestReference = FirebaseDatabase.getInstance().getReference().child("Requests");
        friendReference = FirebaseDatabase.getInstance().getReference().child("Friends");

        loadUser();
        loadMyProfile();

        btnPerform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performAction(userId);
            }
        });

        checkUserExistance(userId);

        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ViewFriendActivity.this, "Decline Btn clicked", Toast.LENGTH_SHORT).show();
                unfriend(userId);
            }
        });
    }

    private void loadMyProfile() {
        userReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    myProfileImageUrl = snapshot.child("profileImage").getValue().toString();
                    myUsername = snapshot.child("username").getValue().toString();
                    myCountry = snapshot.child("country").getValue().toString();
                    myCity = snapshot.child("city").getValue().toString();
                    myProfession = snapshot.child("profession").getValue().toString();

                } else {
                    Toast.makeText(ViewFriendActivity.this, "Data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewFriendActivity.this, "" + error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void unfriend(String userId) {
        Toast.makeText(this, "Inside Unfriend Method", Toast.LENGTH_SHORT).show();
        if (currentState.equals("friend")) {
            Toast.makeText(this, "Current State : Friends", Toast.LENGTH_SHORT).show();
            friendReference.child(firebaseUser.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        friendReference.child(userId).child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ViewFriendActivity.this, "Unfriended", Toast.LENGTH_SHORT).show();
                                    currentState = "nothing_happened";
                                    btnPerform.setText("Send Request");
                                    btnDecline.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }
            });
        }

        if (currentState.equals("he_sent_pending")) {
            Toast.makeText(this, "He sent request", Toast.LENGTH_SHORT).show();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", "decline");
            requestReference.child(userId).child(firebaseUser.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ViewFriendActivity.this, "You have declined Friends Request", Toast.LENGTH_SHORT).show();
                        currentState = "he_sent_decline";
                        btnPerform.setVisibility(View.GONE);
                        btnDecline.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(ViewFriendActivity.this, "Unable to Decline Friends Reqeust", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void checkUserExistance(String userId) {


        //Check if users are friend or not (two methods)
        friendReference.child(firebaseUser.getUid()).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentState = "friend";
                    btnPerform.setText("Send Message");
                    btnDecline.setText("Unfriend");
                    btnDecline.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        friendReference.child(userId).child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentState = "friend";
                    btnPerform.setText("Send Message");
                    btnDecline.setText("Unfriend");
                    btnDecline.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Already sent Friends request (two methods)
        requestReference.child(firebaseUser.getUid()).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("status").getValue().toString().equals("pending")) {
                        currentState = "i_sent_pending";
                        btnPerform.setText("Cancle Friends Request");
                        btnDecline.setVisibility(View.GONE);
                    }

                    if (snapshot.child("status").getValue().toString().equals("decline")) {
                        currentState = "i_sent_decline";
                        btnPerform.setText("Cancle Friends Request");
                        btnDecline.setVisibility(View.GONE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //On receveing request
        requestReference.child(userId).child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("status").getValue().toString().equals("pending")) {
                        currentState = "he_sent_pending";
                        btnPerform.setText("Accept Friends Request");
                        btnDecline.setText("Decline Friends Request");
                        btnDecline.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (currentState.equals("nothing_happened")) {
            currentState = "nothing_happened";
            btnPerform.setText("Send Request");
            btnDecline.setVisibility(View.GONE);
        }
    }

    private void performAction(String userId) {

        //if users have not sent any friend requests
        if (currentState.equals("nothing_happened")) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", "pending");
            requestReference.child(firebaseUser.getUid()).child(userId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ViewFriendActivity.this, "Friends request is sent", Toast.LENGTH_SHORT).show();
                        btnDecline.setVisibility(View.GONE);
                        currentState = "i_sent_pending";
                        btnPerform.setText("Cancel Friends Request");
                    } else {
                        Toast.makeText(ViewFriendActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        //if users have sent requests to each other
        if (currentState.equals("i_sent_pending") || currentState.equals("i_sent_decline")) {
            requestReference.child(firebaseUser.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ViewFriendActivity.this, "Cancled Friends Request", Toast.LENGTH_SHORT).show();
                        currentState = "nothing_happened";
                        btnPerform.setText("Send Request");
                        btnDecline.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(ViewFriendActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


        if (currentState.equals("he_sent_pending")) {
            requestReference.child(userId).child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        final HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("status", "friend");
                        hashMap.put("username", username);
                        hashMap.put("profileImageUrl", profileImageUrl);
                        hashMap.put("profession", profession);

                        final HashMap<String, Object> myHashMap = new HashMap<>();
                        myHashMap.put("status", "friend");
                        myHashMap.put("username", myUsername);
                        myHashMap.put("profileImageUrl", myProfileImageUrl);
                        myHashMap.put("profession", myProfession);


                        friendReference.child(firebaseUser.getUid()).child(userId).updateChildren(hashMap).addOnCompleteListener(task12 -> {
                            if (task12.isSuccessful()) {
                                friendReference.child(userId).child(firebaseUser.getUid()).updateChildren(myHashMap).addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()) {
                                        Log.d("TAG", "Current : " + firebaseUser.getUid());
                                        Log.d("TAG", "Temp : " + userId);
                                        Toast.makeText(ViewFriendActivity.this, "Friends Added ", Toast.LENGTH_SHORT).show();
                                        currentState = "friend";
                                        btnPerform.setText("Send Message");
                                        btnDecline.setText("Unfriend");
                                        btnDecline.setVisibility(View.VISIBLE);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("TAG", "Failed to store myHashMap");
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("TAG", "Failed to store hashMap");
                            }
                        });
                    }
                }
            });

            if (currentState.equals("friend")) {
                Intent intent = new Intent(ViewFriendActivity.this, ChatActivity.class);
                intent.putExtra("otherUserId", userId);
                startActivity(intent);
            }
        }
    }

    private void  loadUser() {
        userReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profileImageUrl = snapshot.child("profileImage").getValue().toString();
                    username = snapshot.child("username").getValue().toString();
                    country = snapshot.child("country").getValue().toString();
                    city = snapshot.child("city").getValue().toString();
                    profession = snapshot.child("profession").getValue().toString();


                    Picasso.get().load(profileImageUrl).into(profileImage);
                    usernameTv.setText(username);
                    addressTv.setText(city + ", " + country);

                } else {
                    Toast.makeText(ViewFriendActivity.this, "Data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewFriendActivity.this, "" + error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}