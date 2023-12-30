package com.jainil.vaatu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
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
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView profileCircleImageView;

    EditText inputUsername, inputCountry, inputCity, inputProfession;
    AppCompatButton updateBtn;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference userReference;
    StorageReference storageReference;

    Uri imageUri;
    private static final int REQUEST_CODE = 101;

    ProgressDialog loadingBarSetup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileCircleImageView = findViewById(R.id.profileActivitycircleImageView);
        inputUsername = findViewById(R.id.profileInputUsernameEt);
        inputCountry = findViewById(R.id.profileInputCountryEt);
        inputCity = findViewById(R.id.profileInputCityEt);
        inputProfession = findViewById(R.id.profiileInputProfessionEt);
        updateBtn = findViewById(R.id.profileUpdateBtn);

        loadingBarSetup = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference().child("profileImage");



        userReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String profileImageUrl = snapshot.child("profileImage").getValue().toString();
                    String username = snapshot.child("username").getValue().toString();
                    String country = snapshot.child("country").getValue().toString();
                    String city = snapshot.child("city").getValue().toString();
                    String profession = snapshot.child("profession").getValue().toString();

                    Picasso.get().load(profileImageUrl).into(profileCircleImageView);
                    inputUsername.setText(username);
                    inputCountry.setText(country);
                    inputCity.setText(city);
                    inputProfession.setText(profession);


                }else{
                    Toast.makeText(ProfileActivity.this, "Data not exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        profileCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });

    }

    private void updateProfile() {

        String username = inputUsername.getText().toString();
        String country = inputCountry.getText().toString();
        String city = inputCity.getText().toString();
        String profession = inputProfession.getText().toString();


        if(username.isEmpty() || username.length() < 3){
            showError(inputUsername, "Username is not valid");
        }else if(country.isEmpty() || country.length() < 3){
            showError(inputCountry, "Country is not valid");
        }else if(city.isEmpty() || city.length() < 3){
            showError(inputCity, "City is not valid");
        }else if(profession.isEmpty() || profession.length() < 3){
            showError(inputProfession, "Profession is not valid");
        }else if(imageUri == null){
            Toast.makeText(ProfileActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
        }else{
            loadingBarSetup.setTitle("Setup Profile");
            loadingBarSetup.setCanceledOnTouchOutside(false);
            loadingBarSetup.show();
            storageReference
                    .child(firebaseUser.getUid())
                    .putFile(imageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                storageReference.child(firebaseUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        HashMap<String,Object> hashMap = new HashMap();
                                        hashMap.put("username",username);
                                        hashMap.put("country",country);
                                        hashMap.put("city",city);
                                        hashMap.put("profession",profession);
                                        hashMap.put("profileImage", uri.toString());
                                        hashMap.put("status", "offline");

                                        userReference.child(firebaseUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                loadingBarSetup.dismiss();
                                                Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                loadingBarSetup.dismiss();
                                                Toast.makeText(ProfileActivity.this, "Error while setting Profile", Toast.LENGTH_SHORT).show();
                                            }
                                        });



                                    }
                                });
                            }


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }


    }
//    private void updateProfile() {
//        String username = inputUsername.getText().toString().trim();
//        String country = inputCountry.getText().toString().trim();
//        String city = inputCity.getText().toString().trim();
//        String profession = inputProfession.getText().toString().trim();
//
//
//        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(country) || TextUtils.isEmpty(city) || TextUtils.isEmpty(profession)) {
//            Toast.makeText(ProfileActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Update the user's profile data in the Firebase Database
//        userReference.child(firebaseUser.getUid()).child("username").setValue(username);
//        userReference.child(firebaseUser.getUid()).child("country").setValue(country);
//        userReference.child(firebaseUser.getUid()).child("city").setValue(city);
//        userReference.child(firebaseUser.getUid()).child("profession").setValue(profession);
//
//        Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            profileCircleImageView.setImageURI(imageUri);
        }

    }
    private void showError(EditText field, String errorMessage) {
        field.setError(errorMessage);
        field.setFocusable(true);
    }
}