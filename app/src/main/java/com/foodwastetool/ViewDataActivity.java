package com.foodwastetool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewDataActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {
    RecyclerView mRecyclerView;
    ImageAdapter mAdapter;
    ProgressBar mProgressCircle;
    FirebaseStorage mStorage;
    FirebaseFirestore mFirestoreRef;
    List<Upload> mUploads;
    public static final String TAG = "TAG";

    public ViewDataActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        mProgressCircle = findViewById(R.id.progress_circle);

        mUploads = new ArrayList<>();
        mAdapter = new ImageAdapter(ViewDataActivity.this, mUploads);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(ViewDataActivity.this);

        mFirestoreRef = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();

        mFirestoreRef.collection("Pictures")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            mUploads.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Upload upload = document.toObject(Upload.class);
                                //upload.setKey(document.getKey());
                                //still figuring out how to delete with firestore
                                mUploads.add(upload);
                                //Log.d(TAG, "The list has these attributes: "+ mUploads);
                            }
                            mAdapter.notifyDataSetChanged();
                            mProgressCircle.setVisibility(View.INVISIBLE);
                           // Log.d(TAG, "i get this far");

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            mProgressCircle.setVisibility(View.INVISIBLE);
                        }
                    }
                });

    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onWhatEverClick(int position) {
        Toast.makeText(this, "Whatever click at position: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {

    }
}
