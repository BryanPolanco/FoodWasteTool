package com.foodwastetool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangeIDActivity extends AppCompatActivity {
    EditText mBuffID;
    Button mUpdateButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_id);
        mBuffID = findViewById(R.id.buffID);
        mUpdateButton = findViewById(R.id.updateButton);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String id = mBuffID.getText().toString();
                final DocumentReference documentReference = fStore.collection("users").document(userID);

                documentReference.update("buffID", id);
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                Toast.makeText(ChangeIDActivity.this, "Buff ID Updated", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
