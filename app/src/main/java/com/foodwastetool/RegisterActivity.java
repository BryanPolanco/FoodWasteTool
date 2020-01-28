package com.foodwastetool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    EditText mFullName, mEmail, mPassword,mVerPassword, mBuffID;
    Button mRegisterButton;
    TextView mAlreadyRegistered;
    FirebaseAuth fireAuth;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFullName = findViewById(R.id.FullName);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mVerPassword = findViewById(R.id.verifyPassword);
        mBuffID = findViewById(R.id.buffID);
        mRegisterButton = findViewById(R.id.RegisterButton);
        mAlreadyRegistered = findViewById(R.id.alreadyRegistered);
        fireAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);


        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //grabbing the values that were inputed by the user
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                //case management
                // if the fields are empty

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is required!");
                    return;
                }
                if(TextUtils.isEmpty(password)) {
                    mPassword.setError("Password id required!");
                    return;
                }
                // if password is less than 6 chars.
                if (password.length() < 6){
                    mPassword.setError("Password must be greater than 6 characters!");
                    return;
                }
                // add maybe a progress bar?
                progressBar.setVisibility(View.VISIBLE);

                //registering the user in Firebase
                fireAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "User Created", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else{
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(RegisterActivity.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        mAlreadyRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });
    }
}
