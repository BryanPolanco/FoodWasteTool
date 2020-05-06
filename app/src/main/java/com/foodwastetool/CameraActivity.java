package com.foodwastetool;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.firestore.admin.v1beta1.Progress;

import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraActivity extends AppCompatActivity {
    public static final int CAMERA_PERM_CODE = 1;
    public static final int CAMERA_REQUEST_CODE = 2;
    public static final int GALLERY_REQUEST_CODE = 5;
    public static final int PICK_IMAGE_REQUEST = 3;
    public static final String TAG = "TAG";
    public int counter = 0;
    String currentPhotoPath;
    ImageView selectedImage;
    Button  cameraButton, galleryButton, uploadButton;
    String userID;

    Uri mImageUri;
    StorageReference  mStorageRef;
    FirebaseFirestore mFirestoreRef;
    FirebaseAuth fireAuth;
    ProgressBar mProgressBar;
    StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        selectedImage = findViewById(R.id.imageView);
        cameraButton = findViewById(R.id.OpenCameraButton);
        galleryButton = findViewById(R.id.galleryButton);
        uploadButton = findViewById(R.id.uploadButton);
        mProgressBar = findViewById(R.id.progressBar4);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mFirestoreRef = FirebaseFirestore.getInstance();
        fireAuth = FirebaseAuth.getInstance();
        userID = fireAuth.getCurrentUser().getUid();
        
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermissions();
            }
        });
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUploadTask != null && mUploadTask.isInProgress()){
                    Toast.makeText(CameraActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadFile();
                }
            }
        });

    }
    private String getFileExxtension(Uri uri) { //only to get image extension from file
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void uploadFile(){
        final String[]  documentData = new String[7];
        final double[] costs = new double[6];
        if (mImageUri != null) {

            FirebaseVisionImage image;
            try {
                image = FirebaseVisionImage.fromFilePath(CameraActivity.this,mImageUri );
                FirebaseVisionCloudImageLabelerOptions options = new FirebaseVisionCloudImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.7f)
                        .build();
                FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                        .getCloudImageLabeler(options);
                labeler.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                                for (FirebaseVisionImageLabel label: labels) {
                                    String text = label.getText();
                                    String entityId = label.getEntityId();
                                    float confidence = label.getConfidence();
                                    Log.d("tag", "the labels in the list are:"+ text +"," + entityId + "," + confidence);

                                    DocumentReference foodRef = mFirestoreRef.collection("Food").document(text);
                                    foodRef.get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();
                                                        if (document.exists()) {
                                                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                                                            String name = document.getString("Food");
                                                            String calories = document.getString("Calories");
                                                            String serving = document.getString("Serving Size");
                                                            String  cost = document.getString("Cost");
                                                            String  water = document.getString("Water Content(oz)");
                                                            Log.d(TAG, "name" + name);
                                                            Log.d(TAG, "calories" + calories);
                                                            Log.d(TAG, "name" + serving);
                                                            documentData[counter] = name;
                                                            counter++;
                                                            documentData[counter] = serving;
                                                            counter++;
                                                            documentData[counter] = calories;
                                                            counter++;
                                                            documentData[counter] = cost;
                                                            counter++;
                                                            documentData[counter] = water;
                                                            counter++;
                                                            Log.d(TAG, "document data array:" + Arrays.toString(documentData));
                                                            System.out.println(documentData[0]);


                                                        } else {
                                                            Log.d(TAG, "No such document");
                                                        }
                                                    } else {
                                                        Log.d(TAG, "get failed with ", task.getException());
                                                    }
                                                }
                                            });
                                }
                                StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() +"." + getFileExxtension(mImageUri));
                                mUploadTask = fileReference.putFile(mImageUri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mProgressBar.setProgress(0);
                                                    }
                                                }, 500);
                                                Toast.makeText(CameraActivity.this, "Upload Succesful", Toast.LENGTH_SHORT).show();

                                                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                                while (!urlTask.isSuccessful());
                                                Uri downloadUrl = urlTask.getResult();

                                                Upload upload = new Upload (downloadUrl.toString());

                                                if (documentData[0] == null){
                                                    DocumentReference documentReference = mFirestoreRef.collection("users").document(userID);
                                                    documentReference.get()
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()){
                                                                        DocumentSnapshot document = task.getResult();
                                                                        if (document.exists()) {
                                                                            String buffID = document.getString("buffID");
                                                                            String firebaseID = userID;
                                                                            documentData[counter] = buffID;
                                                                            counter++;
                                                                            documentData[counter] = firebaseID;
                                                                            counter++;

                                                                        }
                                                                    }
                                                                }
                                                            });
                                                    mFirestoreRef.collection("Pictures").add(upload)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            String docID = documentReference.getId();
                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("name", "undefined");
                                                            data.put("serving", "undefined");
                                                            data.put("calories", "undefined");
                                                            data.put("cost", "undefined");
                                                            data.put("waterContent", "undefined");
                                                            data.put("buffID", documentData[5]);
                                                            data.put("firebaseID", documentData[6]);

                                                            DocumentReference nutritionRef = mFirestoreRef.collection("Pictures").document(docID);
                                                            nutritionRef.update(data)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.w(TAG, "Error updating document", e);
                                                                        }
                                                                    });
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.w(TAG, "Error adding document", e);
                                                                }
                                                            });
                                                }else {
                                                    DateFormat dateFormat = new SimpleDateFormat("MM");
                                                    Date date = new Date();
                                                    String month = dateFormat.format(date);
                                                    DocumentReference monthReference = mFirestoreRef.collection("MonthlyCosts").document(month);
                                                    monthReference.get()
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if(task.isSuccessful()) {
                                                                        DocumentSnapshot document = task.getResult();
                                                                        if(document.exists()) {
                                                                            double monthCost = document.getDouble("dollars");
                                                                            double monthCalories = document.getDouble("calories");
                                                                            double monthWater = document.getDouble("water");


                                                                            costs[0] = monthCost;
                                                                            costs[2] = monthCalories;
                                                                            costs[4] = monthWater;
                                                                            String itemCalories = documentData[2];
                                                                            String itemCost = documentData[3];
                                                                            String itemWater = documentData[4];
                                                                            double doubleItemCalories = Double.parseDouble(itemCalories);
                                                                            double doubleItemCost = Double.parseDouble(itemCost);
                                                                            double doubleItemWater = Double.parseDouble(itemWater);
                                                                            Log.d(TAG, monthCost + " is the month cost variable");
                                                                            Log.d(TAG, monthCalories + " is the month calorie variable");
                                                                            Log.d(TAG, monthWater + " is the month water variable");
                                                                            Log.d(TAG, doubleItemCalories + " is the month cost variable");
                                                                            Log.d(TAG, monthCalories + " is the month calorie variable");
                                                                            Log.d(TAG, monthWater + " is the month water variable");
                                                                            costs[1] = doubleItemCost;
                                                                            costs[3] = doubleItemCalories;
                                                                            costs[5] = doubleItemWater;
                                                                            costs[0] = costs[0] + costs[1];
                                                                            costs[2] = costs[2] + costs[3];
                                                                            costs[4] = costs[4] + costs[5];

                                                                            DateFormat dateFormat = new SimpleDateFormat("MM");
                                                                            Date date = new Date();
                                                                            String month = dateFormat.format(date);
                                                                            DocumentReference monthReference = mFirestoreRef.collection("MonthlyCosts").document(month);
                                                                            double costsZero = Math.round(costs[0]*100)/100;
                                                                            double costsTwo = Math.round(costs[2]*100)/100;
                                                                            double costsFour = Math.round(costs[4]*100)/100;

                                                                            monthReference.update("dollars", costsZero);
                                                                            monthReference.update("calories", costsTwo);
                                                                            monthReference.update("water", costsFour);


                                                                        }
                                                                    }

                                                                }
                                                            });


                                                    Log.d(TAG, "goes into else statement!");
                                                    DocumentReference documentReference = mFirestoreRef.collection("users").document(userID);
                                                    documentReference.get()
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()){
                                                                        DocumentSnapshot document = task.getResult();
                                                                        if (document.exists()) {
                                                                            String buffID = document.getString("buffID");
                                                                            String firebaseID = userID;
                                                                            documentData[counter] = buffID;
                                                                            counter++;
                                                                            documentData[counter] = firebaseID;
                                                                            counter++;

                                                                        }
                                                                    }
                                                                }
                                                            });






                                                    mFirestoreRef.collection("Pictures").add(upload)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            String docID = documentReference.getId();
                                                            Log.d(TAG, "document data array:" + Arrays.toString(documentData));
                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("name", documentData[0]);
                                                            data.put("serving", documentData[1]);
                                                            data.put("calories", documentData[2]);
                                                            data.put("cost", documentData[3]);
                                                            data.put("waterContent", documentData[4]);
                                                            data.put("buffID", documentData[5]);
                                                            data.put("firebaseID", documentData[6]);
                                                            Log.d(TAG, "document data array:" + Arrays.toString(documentData));

                                                            DocumentReference nutritionRef = mFirestoreRef.collection("Pictures").document(docID);
                                                            nutritionRef.update(data)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.w(TAG, "Error updating document", e);
                                                                        }
                                                                    });
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.w(TAG, "Error adding document", e);
                                                                }
                                                            });
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(CameraActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                                double progress = (100.0*taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                                mProgressBar.setProgress((int) progress);
                                            }
                                        });



                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("tag", "Task failed with exception");
                            }
                        });

            }catch(IOException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "no file selected", Toast.LENGTH_SHORT).show();
        }

    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST );
    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERM_CODE);
        }else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else{
                Toast.makeText(this, "Camera permission is needed", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void openCamera(){
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null ) {
            mImageUri = data.getData();
            selectedImage.setImageURI(mImageUri);
        }
        if(requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);
                selectedImage.setImageURI(Uri.fromFile(f));
                //if Picasso was implemented
                //Picasso.with(this).load(mImageUri).into(mImageView);

                Log.d("tag", "ABsolute Url of Image is " + Uri.fromFile(f));

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

            }

        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.foodwastetool.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }
}
