package com.example.deusc.androidblog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;
     private ImageView newPostImage;
     private Button newPostBtn;
     private EditText newPostDesc;
     private Uri postImageUri = null;
     private ProgressBar newPostProgress;
     private StorageReference storageReference;
     private FirebaseFirestore firebaseFirestore;
     private FirebaseAuth firebaseAuth;
     private String currentUserID;
     private Bitmap compressedImageFile;
     private File thumbFile;
    private String LOG_IMAGE = "LOG_IMAGE";
    String imageDownloadUri= null;
    String thumbDownloadUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //firebaseSettup
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();

        newPostImage = findViewById(R.id.new_post_image);
        newPostBtn = findViewById(R.id.post_button);
        newPostDesc = findViewById(R.id.new_post_desc);
        newPostProgress = findViewById(R.id.new_post_progress);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1,1)
                        .start(NewPostActivity.this);
            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String description = newPostDesc.getText().toString();
                if(!TextUtils.isEmpty(description) && postImageUri != null){

                    newPostProgress.setVisibility(View.VISIBLE);
                    final String randomImageName = UUID.randomUUID().toString();

                    File newImageFile = new File(postImageUri.getPath());
                    try {

                        compressedImageFile = new Compressor(NewPostActivity.this)
                                .setMaxHeight(720)
                                .setMaxWidth(720)
                                .setQuality(50)
                                .compressToBitmap(newImageFile);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();

                    UploadTask imagesFilePath = storageReference.child("post_images").child(randomImageName + ".jpg").putBytes(imageData);
                    imagesFilePath.addOnCompleteListener(NewPostActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            //final String downloadURI = task.getResult().getStorage().getDownloadUrl().toString();
                            Task<Uri> urlTask = task.getResult().getStorage().getDownloadUrl();
                            urlTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        Uri imageUri = task.getResult();
                                        imageDownloadUri = imageUri.toString();
                                    }
                                }
                            });
                            if(task.isSuccessful()){
                                //get image Path for compressor
                                thumbFile = new File(postImageUri.getPath());
                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(100)
                                            .setMaxWidth(100)
                                            .setQuality(2)
                                            .compressToBitmap(thumbFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] thumbData = baos.toByteArray();

                                //store compressed image Bitmap to storage
                                final UploadTask uploadTask = storageReference.child("post_images/thumb").child(randomImageName + ".jpg").putBytes(thumbData);
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        /***/
                                        uploadTask.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {
                                                if(task.isSuccessful()) {
                                                    Uri thumbUri = task.getResult();
                                                    thumbDownloadUri = thumbUri.toString();

                                                    File newThumbFile = new File(postImageUri.getPath());
                                                    try {

                                                        compressedImageFile = new Compressor(NewPostActivity.this)
                                                                .setMaxHeight(100)
                                                                .setMaxWidth(100)
                                                                .setQuality(1)
                                                                .compressToBitmap(newThumbFile);

                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                    byte[] thumbData = baos.toByteArray();

                                                    UploadTask uploadTask = storageReference.child("post_images/thumbs")
                                                            .child(randomImageName + ".jpg").putBytes(thumbData);

                                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                                            urlTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Uri> task) {
                                                                    if(task.isSuccessful()){
                                                                        Uri thumbUrl = task.getResult();
                                                                        thumbDownloadUri = thumbUrl.toString();
                                                                    }
                                                                }
                                                            });
                                                            Log.d(LOG_IMAGE, "IMAGE DOWNLOAD URL : " + thumbDownloadUri);

                                                            Map<String, Object> postMap = new HashMap<>();
                                                            postMap.put("image_url", imageDownloadUri);
                                                            postMap.put("thumbnail", thumbDownloadUri);
                                                            //description
                                                            postMap.put("description", description);
                                                            postMap.put("user_id", currentUserID);
                                                            postMap.put("timestamp", FieldValue.serverTimestamp());

                                                            firebaseFirestore.collection("Posts").add(postMap)
                                                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(NewPostActivity.this, "Post was added", Toast.LENGTH_SHORT).show();
                                                                        Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                                        startActivity(mainIntent);
                                                                        finish();
                                                                    } else {
                                                                        String error = task.getException().getMessage().toString();
                                                                        Toast.makeText(NewPostActivity.this, "FIRESTORE Error : " + error, Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    newPostProgress.setVisibility(View.INVISIBLE);
                                                                }
                                                            });
                                                        }
                                                    });
                                                    }
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }else{
                                newPostProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
