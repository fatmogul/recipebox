package com.fatmogul.recipebox;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddEditActivity extends AppCompatActivity {

    private String mUserId;
    private Uri mPhotoDownloadUri;

    private Button mPhotoPickerButton;
    private Button mSaveButton;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private DatabaseReference mRecipeDatabaseReference;
    private StorageReference mRecipePhotoStorageReference;

    private static final int RC_PHOTO_PICKER = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        mUserId = getIntent().getStringExtra("userId");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mRecipeDatabaseReference = mFirebaseDatabase.getReference().child("users/" + mUserId + "/recipes");
        mRecipePhotoStorageReference = mFirebaseStorage.getReference().child("users/" + mUserId + "/photos");

        mPhotoPickerButton = findViewById(R.id.photo_picker_button);
        mSaveButton = findViewById(R.id.save_recipe_button);

        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent,"Complete action using"),RC_PHOTO_PICKER);
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String photoUri = null;
                if(mPhotoDownloadUri != null){
                    photoUri = mPhotoDownloadUri.toString();
                }
                Recipe recipe = new Recipe("Detail Test", "detail test", 1,1, 4, null,null,photoUri, true,null, mUserId);
                mRecipeDatabaseReference.push().setValue(recipe);

            }
        });
    }
    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
        case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        return true;
        default:
        return super.onOptionsItemSelected(item);
    }}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            final StorageReference photoRef = mRecipePhotoStorageReference.child(selectedImageUri.getLastPathSegment());
            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),selectedImageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                byte[] imageData = baos.toByteArray();
                UploadTask uploadTask = photoRef.putBytes(imageData);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return photoRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            mPhotoDownloadUri = task.getResult();
                            ImageView iv = findViewById(R.id.uploaded_photo_iv);
                            Picasso.get().load(mPhotoDownloadUri).fit().centerCrop().into(iv);


                        } else {
                            Toast.makeText(getApplicationContext(),"Photo Upload Failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }


        }}
}
