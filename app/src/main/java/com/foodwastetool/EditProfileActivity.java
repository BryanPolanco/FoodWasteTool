package com.foodwastetool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class EditProfileActivity extends AppCompatActivity {
    TextView mFullName, mEmail, mBuffID;
    Button mGoBack, mChangeName, mChangeEmail, mChangeID, mChangePass;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.email);
        mBuffID = findViewById(R.id.buffID);
        mChangeName = findViewById(R.id.changeNameButton);
        mChangeEmail = findViewById(R.id.changeEmailButton);
        mChangeID = findViewById(R.id.changeBuffIDButton);
        mChangePass = findViewById(R.id.changePassword);
        mGoBack = findViewById(R.id.goBackButton);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();

        final DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                mFullName.setText(documentSnapshot.getString("fullName"));
                mEmail.setText(documentSnapshot.getString("email"));
                mBuffID.setText(documentSnapshot.getString("buffID"));
            }
        });

        mChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ChangeNameActivity.class));
            }
        });
        mChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ChangeEmailActivity.class));
            }
        });
        mChangeID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ChangeIDActivity.class));
            }
        });
        mChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ForgotPassActivity.class));
            }
        });
        mGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MenuActivity.class));
            }
        });
    }
}
