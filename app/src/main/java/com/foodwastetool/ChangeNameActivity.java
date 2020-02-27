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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class ChangeNameActivity extends AppCompatActivity {

    EditText mName;
    Button mUpdateButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);
        mName = findViewById(R.id.name);
        mUpdateButton = findViewById(R.id.updateButton);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();



        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = mName.getText().toString();
                final DocumentReference documentReference = fStore.collection("users").document(userID);
                
                documentReference.update("fullName", name);
                startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                Toast.makeText(ChangeNameActivity.this, "Name Updated", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
