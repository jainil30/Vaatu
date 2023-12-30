package com.jainil.vaatu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jainil.vaatu.Utils.Comment;
import com.jainil.vaatu.Utils.Posts;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE = 101;
    Toolbar toolbar;
    ActionBar actionBar;

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference,postReference,likeReference,commentReference;
    StorageReference postImageStorageReference;

    String usernameV, profileImageUrlV;

    CircleImageView profileImageViewHeader;
    TextView usernameHeader;

    ImageView addImagePost, postImageBtn;
    EditText inputPostDecription;

    Uri imageUri;

    ProgressDialog loadingBar;

    FirebaseRecyclerAdapter<Posts, MyViewHolder> adapter;
    FirebaseRecyclerOptions<Posts> options;

    FirebaseRecyclerOptions<Comment> commentOptions;
    FirebaseRecyclerAdapter<Comment,CommentViewHolder> commentAdapter;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = findViewById(R.id.app_barMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Vaatu");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        addImagePost = findViewById(R.id.addPostImage);
        postImageBtn = findViewById(R.id.publishPostBtn);
        inputPostDecription = findViewById(R.id.inputAddPost);

        drawerLayout = findViewById(R.id.drawerLayoutMainActivity);
        navigationView = findViewById(R.id.navigationViewMainActivity);
        navigationView.setNavigationItemSelectedListener(this);


        View view = navigationView.inflateHeaderView(R.layout.drawer_header);
        profileImageViewHeader = view.findViewById(R.id.profileImageDrawerHeader);
        usernameHeader = view.findViewById(R.id.profileNameDrawerHeader);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        commentReference = FirebaseDatabase.getInstance().getReference().child("Comments");
        postImageStorageReference = FirebaseStorage.getInstance().getReference().child("PostImages");

        loadingBar = new ProgressDialog(this);

        recyclerView = findViewById(R.id.recyclerViewPost);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        postImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPost();
            }
        });

        addImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE );
            }
        });

        loadPost();

    }

    private void loadPost() {
        options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(postReference, Posts.class).build();
        adapter = new FirebaseRecyclerAdapter<Posts, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Posts model) {
                String postKey = getRef(position).getKey();
                holder.username.setText(model.getUsername());
                holder.postDescription.setText(model.getPostDescription());

//                String timeAgoStr = calculateTimeAgo(model.getDatePost());
//                holder.timeAgo.setText(timeAgoStr);

                holder.timeAgo.setText(model.getDatePost());


                Picasso.get().load(model.getPostImageUrl()).into(holder.postImage);
                Picasso.get().load(model.getUserProfileImage()).into(holder.profileImage);

                holder.countLike(postKey,firebaseUser.getUid(),likeReference);
                holder.countComment(postKey,firebaseUser.getUid(),commentReference);

                holder.likesImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        likeReference.child(postKey).child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    likeReference.child(postKey).child(firebaseUser.getUid()).removeValue();
                                    holder.likesImageView.setColorFilter(Color.GRAY);
                                    notifyDataSetChanged();
                                }else{
                                    likeReference.child(postKey).child(firebaseUser.getUid()).setValue("like");
                                    holder.likesImageView.setColorFilter(Color.BLUE);
                                    notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                holder.commentsSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String comment = holder.inputComments.getText().toString();
                        if(comment.isEmpty()){
                            Toast.makeText(MainActivity.this, "Please write something in comments section", Toast.LENGTH_SHORT).show();
                        }else{
                            addComment(holder, postKey, commentReference, firebaseUser.getUid(), comment);
                        }
                    }
                });

//                loadComment(holder,postKey, firebaseUser.getUid());
                loadComment(postKey);
                holder.postImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, ImageViewActivity.class);
                        intent.putExtra("imageUrl", model.getPostImageUrl());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_post,parent,false);
                return new MyViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }

    private void loadComment(String postKey) {
        MyViewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        commentOptions = new FirebaseRecyclerOptions.Builder<Comment>().setQuery(commentReference.child(postKey), Comment.class).build();
        commentAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(commentOptions) {
            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comment model) {
                Picasso.get().load(model.getProfileImageUrl()).into(holder.profileImageView);
                holder.username.setText(model.getUsername());
                holder.comment.setText(model.getComment());
                Log.d("COMMENT", model.getComment());
//                Log.d("COMMENT", model.getProfileImageUrl());
//                Log.d("COMMENT", model.getProfileImageUrl());

            }
            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_comment,parent,false);
                return new CommentViewHolder(view);
            }
        };
        commentAdapter.startListening();
        MyViewHolder.recyclerView.setAdapter(commentAdapter);


    }

//    private void loadComment(MyViewHolder holder, String postKey, String userUid) {
//        holder.recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//
//        commentOptions = new FirebaseRecyclerOptions.Builder<Comment>()
//                .setQuery(commentReference.child(postKey).orderByKey(), Comment.class)
//                .build();
//
//        commentAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(commentOptions) {
//            @Override
//            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comment model) {
//
//                Picasso.get().load(model.getProfileImageUrl()).into(holder.profileImageView);
//                holder.username.setText(model.getUsername());
//                holder.comment.setText(model.getComment());
//            }
//
//            @NonNull
//            @Override
//            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_comment, parent, false);
//                return new CommentViewHolder(view);
//            }
//
//            @Override
//            public long getItemId(int position) {
//                // Override getItemId to return a unique identifier for each item
//                return position;
//            }
//
//            @Override
//            public int getItemViewType(int position) {
//                // Override getItemViewType to return a unique view type for each item
//                return position;
//            }
//        };
//
//        commentAdapter.startListening();
//        MyViewHolder.recyclerView.setAdapter(commentAdapter);
//    }


    private void addComment(MyViewHolder holder, String postKey, DatabaseReference commentReference, String uid, String comment) {
        String commentId = commentReference.child(postKey).push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("username", usernameV);
        hashMap.put("profileImageUrl", profileImageUrlV);
        hashMap.put("comment", comment);

//        commentReference.child(postKey).child(uid).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
        commentReference.child(postKey).child(commentId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    holder.inputComments.setText(null);
                    commentAdapter.notifyDataSetChanged();
                }
                else{
                Toast.makeText(MainActivity.this, "" + task.getException().toString() , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    private String calculateTimeAgo(String datePost) {
//        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
//        try {
//            long time = sdf.parse(datePost).getTime();
//            long now = System.currentTimeMillis();
//            CharSequence ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
//
//            return ago + "";
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return "";
//    }

//    private String calculateTimeAgo(String datePost){
//        try
//        {
//            SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
//            Date past = format.parse(datePost);
//            Date now = new Date();
//            long seconds= TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
//            long minutes=TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
//            long hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
//            long days=TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());
//            if(seconds<60)
//            {
//                return seconds+" seconds ago";
//            }
//            else if(minutes<60)
//            {
//                return minutes+" minutes ago";
//            }
//            else if(hours<24)
//            {
//                return hours+" hours ago";
//            }
//            else
//            {
//                return days+" days ago";
//            }
//        }
//        catch (Exception j){
//            j.printStackTrace();
//        }
//        return "";
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && data != null && resultCode == RESULT_OK){
            imageUri = data.getData();
            addImagePost.setImageURI(imageUri);
        }
    }

    private void addPost() {
        String postDescription = inputPostDecription.getText().toString();
        if(postDescription.isEmpty()){
            inputPostDecription.setError("Please write something in Post Description.");
        }else if (imageUri == null){
            Toast.makeText(MainActivity.this, "Please select an image to post", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Adding Post");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
            String startDate = formatter.format(date);

            postImageStorageReference.child(firebaseUser.getUid() + startDate).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        postImageStorageReference.child(firebaseUser.getUid() + startDate).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                HashMap<String, Object> hashMap = new HashMap();

                                hashMap.put("username",usernameV);
                                hashMap.put("datePost", startDate);
                                hashMap.put("postImageUrl", uri.toString());
                                hashMap.put("postDescription", postDescription);
                                hashMap.put("userProfileImage", profileImageUrlV);

                                postReference.child(firebaseUser.getUid() + startDate).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if(task.isSuccessful()){
                                            loadingBar.dismiss();
                                            Toast.makeText(MainActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                                            addImagePost.setImageResource(R.drawable.ic_add_image);
                                            inputPostDecription.setText(null);

                                        }else{
                                            loadingBar.dismiss();
                                            Toast.makeText(MainActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                        });
                    }
                    else{
                        loadingBar.dismiss();
                        Toast.makeText(MainActivity.this, "" + task.getException().toString() , Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        if(firebaseUser == null){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else{
            databaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        profileImageUrlV = snapshot.child("profileImage").getValue().toString();
                        usernameV = snapshot.child("username").getValue().toString();
                        Picasso.get().load(profileImageUrlV).into(profileImageViewHeader);
                        usernameHeader.setText(usernameV);
                    }else{
                        Toast.makeText(MainActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {



        if(item.getItemId() == R.id.home){
            Toast.makeText(MainActivity.this, "Home" , Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, MainActivity.class));
        }else if(item.getItemId() == R.id.profile){
//            Toast.makeText(MainActivity.this, "Profile" , Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }else if(item.getItemId() == R.id.friend){
//            Toast.makeText(MainActivity.this, "Friends" , Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, FriendActivity.class));
        }else if(item.getItemId() == R.id.findFriend){
//            Toast.makeText(MainActivity.this, "Find Friends" , Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, FindFriendActivity.class));
        }else if(item.getItemId() == R.id.chat){
//            Toast.makeText(MainActivity.this, "Chat" , Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, ChatUsersActivity.class));
        }else if(item.getItemId() == R.id.logout){
            Toast.makeText(MainActivity.this, "Logout" , Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            Intent signoutIntent = new Intent(MainActivity.this, LoginActivity.class);
            signoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(signoutIntent);
            finish();
        }


        return false;
    }
}