package com.jainil.vaatu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jainil.vaatu.Utils.Chat;
import com.squareup.picasso.Picasso;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    EditText inputMessage;
    CircleImageView profileCircleImageView;

    ImageView addSelectImageAttachmentImageView, sendMessageImageView;

    TextView usernameOnAppbarTv, statusOnAppbarTv;


    String otherUserId, otherUsername, otherUserProfileImageLink, otherUserStatus;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference userReference, messageReference;

    FirebaseRecyclerOptions<Chat> options;
    FirebaseRecyclerAdapter<Chat, ChatMyViewHolder> adapter;

    String myProfileImageLink;
    String URL = "https://fcm.googleapis.com/fcm/send";
    RequestQueue requestQueue;

    private static final String CHANNEL_ID = "MessageChannel";
    private static final int NOTIFICATION_ID = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.chatActivityAppbar);
        setSupportActionBar(toolbar);

        otherUserId = getIntent().getStringExtra("otherUserId");

        profileCircleImageView = findViewById(R.id.chatActivityProfileCircleImageView);
        inputMessage = findViewById(R.id.chatActivityMessageEt);
        addSelectImageAttachmentImageView = findViewById(R.id.chatActivitySelectImageAttachmentBtn);
        sendMessageImageView = findViewById(R.id.chatActivityMessageSendBtn);

        usernameOnAppbarTv = findViewById(R.id.userNameTvChatActivityAppbar);
        statusOnAppbarTv = findViewById(R.id.statusTvChatActivityAppBar);

        requestQueue = Volley.newRequestQueue(this);

        recyclerView = findViewById(R.id.chatActivityRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        messageReference = FirebaseDatabase.getInstance().getReference().child("Message");

        loadOtherUser();
        loadMyProfile();

        sendMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        loadMessage();

    }

    private void loadMyProfile() {
        userReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    myProfileImageLink = snapshot.child("profileImage").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessage() {
        options = new FirebaseRecyclerOptions.Builder<Chat>().setQuery(messageReference.child(firebaseUser.getUid()).child(otherUserId), Chat.class).build();
        adapter = new FirebaseRecyclerAdapter<Chat, ChatMyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatMyViewHolder holder, int position, @NonNull Chat model) {
                if (model.getUserId().equals(firebaseUser.getUid())) {
                    holder.firstUserMessageTv.setVisibility(View.GONE);
                    holder.firstProfileCircleImageView.setVisibility(View.GONE);

                    holder.secondUserMessageTv.setVisibility(View.VISIBLE);
                    holder.secondProfileCircleImageView.setVisibility(View.VISIBLE);

                    holder.secondUserMessageTv.setText(model.getMessage());
                    Picasso.get().load(myProfileImageLink).into(holder.secondProfileCircleImageView);
                    sendNotification(model.getMessage());
                } else {
                    holder.firstUserMessageTv.setVisibility(View.VISIBLE);
                    holder.firstProfileCircleImageView.setVisibility(View.VISIBLE);

                    holder.secondUserMessageTv.setVisibility(View.GONE);
                    holder.secondProfileCircleImageView.setVisibility(View.GONE);

                    holder.firstUserMessageTv.setText(model.getMessage());
                    sendNotification(model.getMessage());
                    Picasso.get().load(otherUserProfileImageLink).into(holder.firstProfileCircleImageView);
                }
            }

            @NonNull
            @Override
            public ChatMyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_message, parent, false);
                return new ChatMyViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }

    private void sendMessage() {
        String message = inputMessage.getText().toString();
        if (message.isEmpty()) {
            inputMessage.setError("Please write something");
            inputMessage.setFocusable(true);
        } else {
            HashMap hashMap = new HashMap();
            hashMap.put("message", message);
            hashMap.put("status", "unseen");
            hashMap.put("userId", firebaseUser.getUid());

            messageReference.child(otherUserId).child(firebaseUser.getUid()).push().updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        messageReference.child(firebaseUser.getUid()).child(otherUserId).push().updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    inputMessage.setText(null);
//                                    sendNotification(message);
                                    //notify all baki che aji
                                    Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void sendNotification(String message) {

//        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_message_notification_icon , null);
//        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//        Bitmap largeIcon = bitmapDrawable.getBitmap();

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("otherUserId", otherUserId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,NOTIFICATION_ID, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE );



        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
             notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.icon)
//                    .setLargeIcon(largeIcon)
                    .setContentText(message)
                    .setSubText("New Message from " + otherUsername)
                    .setChannelId(CHANNEL_ID)
                     .setContentIntent(pendingIntent)
                    .build();
//            Toast.makeText(this, "Oreo >" + message, Toast.LENGTH_SHORT).show();
             notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "Message Notification Channel",NotificationManager.IMPORTANCE_HIGH ));
        }else{
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.icon)
//                    .setLargeIcon(largeIcon)
                    .setContentText("New Message from " + otherUsername)
                    .setSubText(message)
                    .setContentIntent(pendingIntent)
                    .build();
//            Toast.makeText(this, "Oreo <" + message, Toast.LENGTH_SHORT).show();
        }




        notificationManager.notify(NOTIFICATION_ID ,notification);



//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("to", "/topics" + otherUserId);
//            JSONObject jsonObject1 = new JSONObject();
//            jsonObject1.put("title", "Message from " + otherUsername);
//            jsonObject1.put("body", message);
//
//            jsonObject.put("notification", jsonObject1);
//
//            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonObject, new Response.Listener<JSONObject>() {
//                @Override
//                public void onResponse(JSONObject response) {
//
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//
//                }
//            }){
//                @Override
//                public Map<String, String> getHeaders() throws AuthFailureError {
//                    HashMap<String, String> map = new HashMap<>();
//                    map.put("content-type","application/json");
//                    map.put("authorization", )
//                    return super.getHeaders();
//                }
//            };
//        } catch (JSONException e) {
//
//        }

    }

    private void loadOtherUser() {
        userReference.child(otherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    otherUsername = snapshot.child("username").getValue().toString();
                    otherUserProfileImageLink = snapshot.child("profileImage").getValue().toString();
                    otherUserStatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(otherUserProfileImageLink).into(profileCircleImageView);
                    usernameOnAppbarTv.setText(otherUsername);
                    statusOnAppbarTv.setText(otherUserStatus);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}