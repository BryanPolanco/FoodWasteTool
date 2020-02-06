package com.foodwastetool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText mFullName, mEmail, mPassword,mVerPassword, mBuffID;
    Button mRegisterButton;
    TextView mAlreadyRegistered;
    FirebaseAuth fireAuth;

    ProgressBar progressBar;
    String userID;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();



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
                final String email = mEmail.getText().toString().trim();
                String verPassword = mVerPassword.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                final String fullName = mFullName.getText().toString();
                final String buffID = mBuffID.getText().toString();

                //case management
                // if the fields are empty
                if(TextUtils.isEmpty(fullName)){
                    mFullName.setError("Name is required!");
                    return;
                }
                if(TextUtils.isEmpty(buffID)){
                    mBuffID.setError("Buff ID is required!");
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is required!");
                    return;
                }
                if(TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is required!");
                    return;
                }
                if(TextUtils.isEmpty(verPassword)) {
                    mVerPassword.setError("Verification Password is required!");
                    return;
                }
                if(!password.equals(verPassword)) {
                    mPassword.setError("Passwords do not Match!");
                    mVerPassword.setError("Passwords do not Match!");
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
                FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                        .setTimestampsInSnapshotsEnabled(true)
                        .build();
                firestore.setFirestoreSettings(settings);
                fireAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "User Created", Toast.LENGTH_SHORT).show();

                            userID = fireAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = firestore.collection("users").document(userID);

                            Map<String,Object> user = new HashMap<>();
                            user.put("buffID", buffID);
                            user.put("fullName", fullName);
                            user.put("email", email);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: Profile created for " + userID);
                                }

                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: "+ e.toString());
                                }
                            });

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
