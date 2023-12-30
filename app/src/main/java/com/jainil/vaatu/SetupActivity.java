package com.jainil.vaatu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    Toolbar toolbar;

    private static final int REQUEST_CODE = 101;
    TextInputEditText inputUsername, inputCountry, inputCity, inputProfession;
    CircleImageView profileImageView;

    AppCompatButton saveBtn;
    Uri imageUri;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    StorageReference storageReference;

    ProgressDialog loadingBarSetup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Setup Profile");



        profileImageView = findViewById(R.id.profileCircleImageViewSetup);

        inputUsername = findViewById(R.id.usernameEtSetup);
        inputCountry = findViewById(R.id.countryEtSetup);
        inputCity = findViewById(R.id.cityEtSetup);
        inputProfession = findViewById(R.id.professionEtSetup);

        saveBtn = findViewById(R.id.saveBtnSetup);


        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference().child("profileImage");



        loadingBarSetup = new ProgressDialog(this);


        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }

    }
    private void saveData() {

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
            Toast.makeText(SetupActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
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

                                databaseReference.child(firebaseUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                      public void onSuccess(Void unused) {
                                        loadingBarSetup.dismiss();
                                        Toast.makeText(SetupActivity.this, "Setup Profile Complete", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadingBarSetup.dismiss();
                                        Toast.makeText(SetupActivity.this, "Error while setting Profile", Toast.LENGTH_SHORT).show();
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

    private void showError(TextInputEditText field, String errorMessage) {
        field.setError(errorMessage);
        field.setFocusable(true);
    }


}