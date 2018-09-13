package com.fatmogul.recipebox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.List;

public class AddEditActivity extends AppCompatActivity {

    private static final int RC_PHOTO_PICKER = 2;
    private static ArrayList mIngredients;
    private static ArrayList mDirections;
    private static IngredientAdapter mIngredientAdapter;
    private static DirectionAdapter mDirectionAdapter;
    private String mUserId;
    private Uri mPhotoDownloadUri;
    private String mTaskId;
    private Button mPhotoPickerButton;
    private Button mSaveButton;
    private Button mClearButton;
    private Button mCancelButton;
    private ImageView mImageView;
    private EditText mRecipeTitleEditText;
    private EditText mPrepTimeEditEdit;
    private EditText mCookTimeEditText;
    private EditText mServesEditText;
    private CheckBox mFavoritesCheckBox;
    private Button mAddIngredientButton;
    private Button mAddDirectionButton;
    private ListView mIngredientListView;
    private ListView mDirectionsListView;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private DatabaseReference mRecipeDatabaseReference;
    private StorageReference mRecipePhotoStorageReference;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("ingredients",mIngredients);
        outState.putParcelableArrayList("directions",mDirections);
        if(mPhotoDownloadUri != null){
        outState.putString("photoUriString",mPhotoDownloadUri.toString());}
        super.onSaveInstanceState(outState);
    }

    public static void removeIngredient(int position) {
        if(mIngredients.size() == 1){
            mIngredients.add(new Ingredient(0,null,"None"));
        }
        mIngredients.remove(position);
        mIngredientAdapter.notifyDataSetChanged();
    }
    public static void removeDirection(int position) {
        if(mDirections.size() == 1){
            mDirections.add(new Direction("None"));
        }
        mDirections.remove(position);
        mDirectionAdapter.notifyDataSetChanged();
    }

    public void addIngredient() {

        LayoutInflater inflater = getLayoutInflater();
        final View dialogBox = inflater.inflate(R.layout.add_ingredient_dialog, null);

        AlertDialog dialog = new AlertDialog.Builder(AddEditActivity.this)
                .setTitle("Add a new Ingredient")
                .setView(dialogBox)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText qtyBox = dialogBox.findViewById(R.id.quantity_add_edit_text);
                        EditText measurementBox = dialogBox.findViewById(R.id.measurement_add_edit_text);
                        EditText ingredientBox = dialogBox.findViewById(R.id.ingredient_add_edit_text);
                        if(!qtyBox.getText().toString().equals("") && !measurementBox.getText().toString().equals("") && !ingredientBox.toString().equals("")){


                        mIngredients.add(new Ingredient(Long.parseLong(qtyBox.getText().toString()),
                                measurementBox.getText().toString(), ingredientBox.getText().toString()));
                        Ingredient firstIngredient = (Ingredient) mIngredients.get(0);
                        if(mIngredients.size() > 0 && firstIngredient.getIngredient().equals("None")){
                            mIngredients.remove(0);
                        }
                        mIngredientAdapter.notifyDataSetChanged();
                            }}
                })
                .setNeutralButton("Add another", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText qtyBox = dialogBox.findViewById(R.id.quantity_add_edit_text);
                        EditText measurementBox = dialogBox.findViewById(R.id.measurement_add_edit_text);
                        EditText ingredientBox = dialogBox.findViewById(R.id.ingredient_add_edit_text);

                        mIngredients.add(new Ingredient(Long.parseLong(qtyBox.getText().toString()),
                                measurementBox.getText().toString(), ingredientBox.getText().toString()));
                        Ingredient firstIngredient = (Ingredient) mIngredients.get(0);
                        if(mIngredients.size() > 0 && firstIngredient.getIngredient().equals("None")){
                            mIngredients.remove(0);
                        }
                        mIngredientAdapter.notifyDataSetChanged();
                        addIngredient();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    public void addDirection() {
final EditText directionEditText = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(AddEditActivity.this)
                .setTitle("Add a new Direction")
                .setView(directionEditText)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!directionEditText.getText().toString().equals("")){
                            mDirections.add(new Direction(directionEditText.getText().toString()));
                            Direction firstDirection = (Direction) mDirections.get(0);
                            if(mDirections.size() > 0 && firstDirection.getDirectionText().equals("None")){
                                mDirections.remove(0);
                            }
                            mDirectionAdapter.notifyDataSetChanged();
                        }}
                })
                .setNeutralButton("Add another", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDirections.add(new Direction(directionEditText.getText().toString()));
                        Direction firstDirection = (Direction) mDirections.get(0);
                        if(mDirections.size() > 0 && firstDirection.getDirectionText().equals("None")){
                            mDirections.remove(0);
                        }
                        mDirectionAdapter.notifyDataSetChanged();
                        addDirection();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }


    public static void updateDirection(final int position, Activity context) {
        final EditText directionBox = new EditText(context);
        Direction thisDirection = (Direction) mDirections.get(position);
        directionBox.setText(thisDirection.getDirectionText());
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Update Direction")
                .setView(directionBox)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!directionBox.getText().toString().equals("")){
                            mDirections.set(position,new Direction(directionBox.getText().toString()));
                            Direction firstDirection = (Direction) mDirections.get(0);
                            if(mDirections.size() > 0 && firstDirection.getDirectionText().equals("None")){
                                mDirections.remove(0);
                            }
                            mDirectionAdapter.notifyDataSetChanged();
                        }}
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
        }
    public static void updateIngredient(final int position, Activity context) {
        LayoutInflater inflater = context.getLayoutInflater();
        final View dialogBox = inflater.inflate(R.layout.add_ingredient_dialog, null);
        final EditText qtyBox = dialogBox.findViewById(R.id.quantity_add_edit_text);
        final EditText measurementBox = dialogBox.findViewById(R.id.measurement_add_edit_text);
        final EditText ingredientBox = dialogBox.findViewById(R.id.ingredient_add_edit_text);
        Ingredient thisIngredient = (Ingredient) mIngredients.get(position);
        qtyBox.setText(String.valueOf(thisIngredient.getQuantity()));
        measurementBox.setText(thisIngredient.getMeasurement());
        ingredientBox.setText(thisIngredient.getIngredient());
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Update Ingredient")
                .setView(dialogBox)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!qtyBox.getText().toString().equals("") && !measurementBox.getText().toString().equals("") && !ingredientBox.toString().equals("")){


                            mIngredients.set(position,new Ingredient(Long.parseLong(qtyBox.getText().toString()),
                                    measurementBox.getText().toString(), ingredientBox.getText().toString()));
                            Ingredient firstIngredient = (Ingredient) mIngredients.get(0);
                            if(mIngredients.size() > 0 && firstIngredient.getIngredient().equals("None")){
                                mIngredients.remove(0);
                            }
                            mIngredientAdapter.notifyDataSetChanged();
                        }}
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        mImageView = findViewById(R.id.uploaded_photo_iv);
        mRecipeTitleEditText = findViewById(R.id.recipe_title_edit_text);
        mPrepTimeEditEdit = findViewById(R.id.prep_time_edit_text);
        mCookTimeEditText = findViewById(R.id.cook_time_edit_text);
        mServesEditText = findViewById(R.id.servings_edit_text);
        mFavoritesCheckBox = findViewById(R.id.favorites_check_box);
        mAddIngredientButton = findViewById(R.id.add_ingredient_button);
        mAddDirectionButton = findViewById(R.id.add_direction_button);
        mRecipeTitleEditText.clearFocus();
        mUserId = getIntent().getStringExtra("userId");
        mTaskId = getIntent().getStringExtra("taskId");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mRecipeDatabaseReference = mFirebaseDatabase.getReference().child("users/" + mUserId + "/recipes");
        mRecipePhotoStorageReference = mFirebaseStorage.getReference().child("users/" + mUserId + "/photos");

        mIngredientListView = findViewById(R.id.ingredients_list_view);
        if(savedInstanceState != null){
            mIngredients = savedInstanceState.getParcelableArrayList("ingredients");
            try {
                mPhotoDownloadUri = Uri.parse(savedInstanceState.getString("photoUriString"));
            }catch (Exception e){
                mPhotoDownloadUri = null;
            }
            }else{
        mIngredients = new ArrayList<>();
        mIngredients.add(new Ingredient(0,null,getString(R.string.none_loaded)));}
        mIngredientAdapter = new IngredientAdapter(this, R.layout.ingredient_display_list_view, mIngredients);
        mIngredientListView.setAdapter(mIngredientAdapter);

        mDirectionsListView = findViewById(R.id.directions_list_view);
        if(savedInstanceState != null){
            mDirections = savedInstanceState.getParcelableArrayList("directions");
        }else{
        mDirections = new ArrayList<>();
        mDirections.add(new Direction(getString(R.string.none_loaded)));}
        mDirectionAdapter = new DirectionAdapter(this, R.layout.direction_display_list_view, mDirections);
        mDirectionsListView.setAdapter(mDirectionAdapter);


        if (mTaskId.equals("new")) {
            setTitle("Add New Recipe");
        } else {
            //TODO: get recipe name from database for title
        }
        mPhotoPickerButton = findViewById(R.id.photo_picker_button);
        mSaveButton = findViewById(R.id.save_recipe_button);
        mClearButton = findViewById(R.id.clear_button);
        mCancelButton = findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String photoUri = null;
                String recipeTitle;
                long prepTime = 0;
                long cookTime = 0;
                long servings = 0;
                if (mPhotoDownloadUri != null) {
                    photoUri = mPhotoDownloadUri.toString();
                }

                boolean favoriteSelection = mFavoritesCheckBox.isChecked();
                List missingData = new ArrayList();
                recipeTitle = mRecipeTitleEditText.getText().toString();
                if (recipeTitle.equals("")) {
                    missingData.add(R.string.recipe_name);
                }
                try {
                    prepTime = Long.parseLong(mPrepTimeEditEdit.getText().toString());
                } catch (Exception e) {
                    missingData.add(R.string.prep_time);
                }
                try {
                    cookTime = Long.parseLong(mCookTimeEditText.getText().toString());
                } catch (Exception e) {
                    missingData.add(R.string.cook_time);
                }
                try {
                    servings = Long.parseLong(mServesEditText.getText().toString());
                } catch (Exception e) {
                    missingData.add(R.string.serves);
                }
                if (missingData.size() > 0) {
                    String toastMessage = getString(R.string.add_edit_save_error_toast_message);
                    for (Object string : missingData) {
                        toastMessage = toastMessage + "\n" + string.toString();
                    }
                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
                } else {
                    Recipe recipe = new Recipe(recipeTitle,
                            recipeTitle.toLowerCase(),
                            prepTime,
                            cookTime,
                            servings,
                            mIngredients,
                            mDirections,
                            photoUri,
                            favoriteSelection,
                            null,
                            mUserId);
                    mRecipeDatabaseReference.push().setValue(recipe);
                    finish();
//TODO: handle screen rotation and pauses and such
                }
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mAddIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addIngredient();
            }
        });
        mAddDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDirection();
            }
        });
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoDownloadUri = null;
                setPicture();
            }
        });
        setPicture();
    }


    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            final StorageReference photoRef = mRecipePhotoStorageReference.child(selectedImageUri.getLastPathSegment());
            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
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
                            setPicture();

                        } else {
                            Toast.makeText(getApplicationContext(), "Photo Upload Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    public void setPicture() {
        Picasso.get().load(mPhotoDownloadUri).fit().centerCrop().placeholder(R.drawable.ic_add_a_photo_grey_24dp).into(mImageView);
        if (mPhotoDownloadUri != null) {
            mPhotoPickerButton.setText(R.string.change_photo);
            mClearButton.setVisibility(View.VISIBLE);
        } else {
            mPhotoPickerButton.setText(R.string.add_photo);
            mClearButton.setVisibility(View.GONE);
        }

    }
}
