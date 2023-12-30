package com.jainil.vaatu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    TextInputEditText emailId,passwordEmail,confromPassword;
    AppCompatButton registerBtn;

    TextView haveAccountTxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);



        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Create Account");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }


        mAuth = FirebaseAuth.getInstance();

        emailId = findViewById(R.id.emailIdEt);
        passwordEmail = findViewById(R.id.passwordEt);
        registerBtn = findViewById(R.id.registerBtn);
        confromPassword = findViewById(R.id.conformPasswordEt);




        haveAccountTxt = findViewById(R.id.haveAccountTv);

        progressDialog = new ProgressDialog(this);


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 String email = emailId.getText().toString();
                 String password = passwordEmail.getText().toString();
                 String conformPassword = confromPassword.getText().toString();


                 if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                     //Showing error if email is not valid
                     emailId.setError("Invalid Email");
                     emailId.setFocusable(true);
                 }
                 else if (password.length() < 6) {
                     //Showing error if password is less than 6 words
                     passwordEmail.setError("Password length must be at least 6 characters");
                     passwordEmail.setFocusable(true);

                 }
                 else if(!password.equals(conformPassword)){
                     confromPassword.setError("Password and Conform Password does not match");
                     confromPassword.setFocusable(true);
                 }
                 else{
                     //Registering user if the email and password passes validations
                     progressDialog.setTitle("Registering User...");
                     progressDialog.setMessage("Please wait, while your credentials are registered ");
                     progressDialog.setCanceledOnTouchOutside(false);
                     registerUser(email, password);
                 }
            }
        });


        haveAccountTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

    }

    private void registerUser(String email, String password){

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            progressDialog.dismiss();

                            Toast.makeText(RegisterActivity.this,"Registration Successfull",Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(RegisterActivity.this, SetupActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();


                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Registration failed",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();

        return super.onSupportNavigateUp();
    }
}