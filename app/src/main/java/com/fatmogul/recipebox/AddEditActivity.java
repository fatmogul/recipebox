package com.fatmogul.recipebox;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
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
import java.util.Objects;

import static com.fatmogul.recipebox.MainActivity.DIRECTIONS;
import static com.fatmogul.recipebox.MainActivity.INGREDIENTS;
import static com.fatmogul.recipebox.MainActivity.PHOTO_URI;

/*
This activity serves the dual purpose of working as a screen for adding a new recipe as well as
allowing to edit existing recipes.

    @RC_PHOTO_PICKER
        Provides the id for the photo picker process
    @mIngredients
        The ArrayList of all ingredients for the recipe.
    @mDirections
        The ArrayList of all directions for the recipe.
    @mIngredientAdapter
        The local IngredientAdapter for presenting ingredients in the activity.
    @mDirectionAdapter
         The local DirectionAdapter for presenting directions in the activity.
    @mUserId
        The local housing for the user's firebase auth id, which allows them to access their saved
            recipes as well as connected photos.
    @mPhotoDownloadUri
        Holds the Uri for the Photo if it has been chosen.
    @mTaskId
        Captures the extra string from the intent to determine if this activity is intended to add a
            new recipe or edit an existing one.
        "new" if new Recipe
        "edit" if existing.
    @mPhotoPickerButton
        The 'Add Photo' button on the activity_add_edit xml, which opens the photo picker intent
    @mSaveButton
        The Save Button on the activity_add_edit xml which saved the recipe to the firebase database
    @mClearButton
        The Clear Button which appears when a photo is showing in the photo image view.
    @mDeleteButton
        THe Delete Recipe button which allows to remove recipes from the firebase database.
    @mImageView
        The imageview which holds the image connected to the recipe, is populated from the database
            when this is an already existing recipe.
    @mRecipeTitleEditText
        THe EditText field which holds the name of the recipe, is populated from the database when
            this is an already existing recipe.
    @mPrepTimeEditText
        The EditText field which holds the Prep Time of the recipe, populated from the database when
            this is an already existing recipe.
    @mCookTimeEditText
        The EditText field which holds the Cook Time of the recipe, populated from the database when
            this is an already existing recipe.
    @mServesEditText
        The EditText field which holds the Servings of the recipe, populated from the database when
            this is an already existing recipe.
    @mFavoritesCheckBox
        The CheckBox field which is checked if this recipe has been declared as a recipe.  Populated
            from database if this is a previously existing recipe.
    @mAddIngredientButton
        The AddIngredient Button from the activity_add_edit xml which adds a new ingredient line
            to the view.
    @mAddDirectionButton
        The AddDirection Button from the activity_add_edit xml which adds a new direction line to
            the view.
    @mIngredientListView
        The ListView which is populated by the IngredientAdapter using the ingredient_display_list_view
            xml.
    @mDirectionListView
        THe ListView which is populated by the DirectionAdapter using the direction_display_list_view
            xml.
    @mFirebaseDatabase
        Holds the Firebase Database instance.
    @mFirebaseStorage
        Holds the FIrebase Storage instance.
    @mRecipeDatabaseReference
        Holds the mFirebaseDatabase Reference to allow for changes to database.
    @mRecipe
        Holds the detail for the current Recipe, obtained from the intent if this is an existing recipe.
    @mRecipeId
        Holds the ID for the current Recipe, obtained from the intent if this is an existing recipe.
    @mRecipePhotoStorageReference
        Holds the storage reference for mFirebaseStorage to allow for changes to file storage.
    @mIngredientBlogList
        A simple string which populates the full list of ingredients to create a searchable object
            for the main screen.
 */

public class AddEditActivity extends AppCompatActivity {

    private static final int RC_PHOTO_PICKER = 2;
    private static ArrayList<Ingredient> mIngredients;
    private static ArrayList<Direction> mDirections;
    private static IngredientAdapter mIngredientAdapter;
    private static DirectionAdapter mDirectionAdapter;
    private String mUserId;
    private Uri mPhotoDownloadUri;
    private String mTaskId;
    private Button mPhotoPickerButton;
    private Button mClearButton;
    private ImageView mImageView;
    private EditText mRecipeTitleEditText;
    private EditText mPrepTimeEditText;
    private EditText mCookTimeEditText;
    private EditText mServesEditText;
    private CheckBox mFavoritesCheckBox;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRecipeDatabaseReference;
    private Recipe mRecipe;
    private String mRecipeId;
    private StorageReference mRecipePhotoStorageReference;
    private String mIngredientBlobList;

    /*
    Simple module for removing an ingredient from the ingredients ArrayList and updating the adapter.
     */
    public static void removeIngredient(int position) {
        if (mIngredients.size() == 1) {
            mIngredients.add(new Ingredient(0, null, Resources.getSystem().getString(R.string.none_loaded)));
        }
        mIngredients.remove(position);
        mIngredientAdapter.notifyDataSetChanged();
    }

    /*
    Simple module for removing a direction from the directions ArrayList and updating the adapter
     */
    public static void removeDirection(int position) {
        if (mDirections.size() == 1) {
            mDirections.add(new Direction(Resources.getSystem().getString(R.string.none_loaded)));
        }
        mDirections.remove(position);
        mDirectionAdapter.notifyDataSetChanged();
    }

    /*
    Module which creates a dialog box for the purposes of updating a Direction when the edit button
        is clicked.
     */
    public static void updateDirection(final int position, Activity context) {
        final EditText directionBox = new EditText(context);
        Direction thisDirection = mDirections.get(position);
        directionBox.setText(thisDirection.getDirectionText());
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(Resources.getSystem().getString(R.string.update_direction))
                .setView(directionBox)
                .setPositiveButton(Resources.getSystem().getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!directionBox.getText().toString().equals("")) {
                            mDirections.set(position, new Direction(directionBox.getText().toString()));
                            Direction firstDirection = mDirections.get(0);
                            if (firstDirection.getDirectionText().equals(Resources.getSystem().getString(R.string.none_loaded))) {
                                mDirections.remove(0);
                            }
                            mDirectionAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton(Resources.getSystem().getString(R.string.cancel), null)
                .create();
        dialog.show();
    }

    /*
    Module which creates a dialog box for the purposes of updating an Ingredient when the edit button
        is clicked.
    */
    public static void updateIngredient(final int position, Activity context) {
        LayoutInflater inflater = context.getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogBox = inflater.inflate(R.layout.add_ingredient_dialog, null);
        final EditText qtyBox = dialogBox.findViewById(R.id.quantity_add_edit_text);
        final EditText measurementBox = dialogBox.findViewById(R.id.measurement_add_edit_text);
        final EditText ingredientBox = dialogBox.findViewById(R.id.ingredient_add_edit_text);
        Ingredient thisIngredient = mIngredients.get(position);
        qtyBox.setText(String.valueOf(thisIngredient.getQuantity()));
        measurementBox.setText(thisIngredient.getMeasurement());
        ingredientBox.setText(thisIngredient.getIngredient());
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(Resources.getSystem().getString(R.string.update_ingredient))
                .setView(dialogBox)
                .setPositiveButton(Resources.getSystem().getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!qtyBox.getText().toString().equals("") && !measurementBox.getText().toString().equals("") && !ingredientBox.toString().equals("")) {
                            mIngredients.set(position, new Ingredient(Long.parseLong(qtyBox.getText().toString()),
                                    measurementBox.getText().toString(), ingredientBox.getText().toString()));
                            Ingredient firstIngredient = mIngredients.get(0);
                            if (firstIngredient.getIngredient().equals(Resources.getSystem().getString(R.string.none_loaded))) {
                                mIngredients.remove(0);
                            }
                            mIngredientAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton(Resources.getSystem().getString(R.string.cancel), null)
                .create();
        dialog.show();
    }

    /*
         save the ingredients and directions ArrayLists into memory, as well as the mPhotoDownloadUri if available.
    */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(INGREDIENTS, mIngredients);
        outState.putParcelableArrayList(DIRECTIONS, mDirections);
        if (mPhotoDownloadUri != null) {
            outState.putString(PHOTO_URI, mPhotoDownloadUri.toString());
        }
        super.onSaveInstanceState(outState);
    }

    /*
    If the Add Ingredient button is pressed, it calls this module to open a dialog box to collect the
        data for the new ingredient.
     */
    private void addIngredient() {
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogBox = inflater.inflate(R.layout.add_ingredient_dialog, null);

        AlertDialog dialog = new AlertDialog.Builder(AddEditActivity.this)
                .setTitle(Resources.getSystem().getString(R.string.add_ingredient))
                .setView(dialogBox)
                .setPositiveButton(Resources.getSystem().getString(R.string.done), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText qtyBox = dialogBox.findViewById(R.id.quantity_add_edit_text);
                        EditText measurementBox = dialogBox.findViewById(R.id.measurement_add_edit_text);
                        EditText ingredientBox = dialogBox.findViewById(R.id.ingredient_add_edit_text);
                        if (!qtyBox.getText().toString().equals("") && !measurementBox.getText().toString().equals("") && !ingredientBox.toString().equals("")) {


                            mIngredients.add(new Ingredient(Long.parseLong(qtyBox.getText().toString()),
                                    measurementBox.getText().toString(), ingredientBox.getText().toString()));
                            Ingredient firstIngredient = mIngredients.get(0);
                            if (firstIngredient.getIngredient().equals(Resources.getSystem().getString(R.string.none_loaded))) {
                                mIngredients.remove(0);
                            }
                            mIngredientAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .setNeutralButton(Resources.getSystem().getString(R.string.add_another), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText qtyBox = dialogBox.findViewById(R.id.quantity_add_edit_text);
                        EditText measurementBox = dialogBox.findViewById(R.id.measurement_add_edit_text);
                        EditText ingredientBox = dialogBox.findViewById(R.id.ingredient_add_edit_text);

                        mIngredients.add(new Ingredient(Long.parseLong(qtyBox.getText().toString()),
                                measurementBox.getText().toString(), ingredientBox.getText().toString()));
                        Ingredient firstIngredient = mIngredients.get(0);
                        if (firstIngredient.getIngredient().equals(Resources.getSystem().getString(R.string.none_loaded))) {
                            mIngredients.remove(0);
                        }
                        mIngredientAdapter.notifyDataSetChanged();
                        addIngredient();
                    }
                })
                .setNegativeButton(Resources.getSystem().getString(R.string.cancel), null)
                .create();
        dialog.show();
    }

    /*
    If the Add Direction button is pressed, it calls this module to open a dialog box to collect the
        data for the new ingredient.
     */
    private void addDirection() {
        final EditText directionEditText = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(AddEditActivity.this)
                .setTitle(Resources.getSystem().getString(R.string.add_direction))
                .setView(directionEditText)
                .setPositiveButton(Resources.getSystem().getString(R.string.done), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!directionEditText.getText().toString().equals("")) {
                            mDirections.add(new Direction(directionEditText.getText().toString()));
                            Direction firstDirection = mDirections.get(0);
                            if (firstDirection.getDirectionText().equals(Resources.getSystem().getString(R.string.none_loaded))) {
                                mDirections.remove(0);
                            }
                            mDirectionAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .setNeutralButton(Resources.getSystem().getString(R.string.add_another), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDirections.add(new Direction(directionEditText.getText().toString()));
                        Direction firstDirection = mDirections.get(0);
                        if (firstDirection.getDirectionText().equals(Resources.getSystem().getString(R.string.none_loaded))) {
                            mDirections.remove(0);
                        }
                        mDirectionAdapter.notifyDataSetChanged();
                        addDirection();
                    }
                })
                .setNegativeButton(Resources.getSystem().getString(R.string.cancel), null)
                .create();
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        mImageView = findViewById(R.id.uploaded_photo_iv);
        mRecipeTitleEditText = findViewById(R.id.recipe_title_edit_text);
        mPrepTimeEditText = findViewById(R.id.prep_time_edit_text);
        mCookTimeEditText = findViewById(R.id.cook_time_edit_text);
        mServesEditText = findViewById(R.id.servings_edit_text);
        mFavoritesCheckBox = findViewById(R.id.favorites_check_box);
        Button mAddIngredientButton = findViewById(R.id.add_ingredient_button);
        Button mAddDirectionButton = findViewById(R.id.add_direction_button);
        Button mDeleteButton = findViewById(R.id.delete_recipe_button);
        mRecipeTitleEditText.clearFocus();
        mUserId = getIntent().getStringExtra(MainActivity.USER_ID);
        mTaskId = getIntent().getStringExtra(MainActivity.TASK_ID);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
        mRecipeId = null;
        mIngredientBlobList = null;
        /*
        The "Edit" taskId, which comes from the intent to this activity, designates that we are
        editing an existing recipe, which causes the app to populate the fields with the existing
        data vs the "new" taskId, which designates that this is a new recipe, which keeps all fields
        empty.
         */
        if (mTaskId.equals(MainActivity.EDIT)) {
            mRecipe = getIntent().getParcelableExtra(MainActivity.RECIPE);
            setTitle(R.string.edit + R.string.blank_space + mRecipe.getTitle());
            mDeleteButton.setVisibility(View.VISIBLE);
            mRecipeId = mRecipe.getRecipeId();
            mFavoritesCheckBox.setChecked(mRecipe.isFavorite());
            mIngredients = getIntent().getParcelableArrayListExtra(INGREDIENTS);
            mDirections = getIntent().getParcelableArrayListExtra(DIRECTIONS);
            try {
                mPhotoDownloadUri = Uri.parse(mRecipe.getPhotoUrl());
            } catch (Exception e) {
            }
            mRecipeTitleEditText.setText(mRecipe.getTitle());
            mPrepTimeEditText.setText(String.valueOf(mRecipe.getPrepTime()));
            mCookTimeEditText.setText(String.valueOf(mRecipe.getCookTime()));
            mServesEditText.setText(String.valueOf(mRecipe.getServings()));
        } else {
            setTitle(R.string.add_recipe);
        }
        if (savedInstanceState != null) {
            mIngredients = savedInstanceState.getParcelableArrayList(INGREDIENTS);
            mDirections = savedInstanceState.getParcelableArrayList(DIRECTIONS);
            try {
                mPhotoDownloadUri = Uri.parse(savedInstanceState.getString(PHOTO_URI));
            } catch (Exception e) {
                mPhotoDownloadUri = null;
            }
        }
        mRecipeDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.users_path_segment) + mUserId + getString(R.string.recipes_path_segment));
        mRecipePhotoStorageReference = mFirebaseStorage.getReference().child(getString(R.string.users_path_segment) + mUserId + getString(R.string.photos_path_segment));
        ListView mIngredientListView = findViewById(R.id.ingredients_list_view);

        if (mIngredients == null) {
            mIngredients = new ArrayList<>();
            mIngredients.add(new Ingredient(0, null, getString(R.string.none_loaded)));
        }
        mIngredientAdapter = new IngredientAdapter(this, R.layout.ingredient_display_list_view, mIngredients);
        mIngredientListView.setAdapter(mIngredientAdapter);

        ListView mDirectionsListView = findViewById(R.id.directions_list_view);
        if (mDirections == null) {
            mDirections = new ArrayList<>();
            mDirections.add(new Direction(getString(R.string.none_loaded)));
        }
        mDirectionAdapter = new DirectionAdapter(this, R.layout.direction_display_list_view, mDirections);
        mDirectionsListView.setAdapter(mDirectionAdapter);

        mPhotoPickerButton = findViewById(R.id.photo_picker_button);
        Button mSaveButton = findViewById(R.id.save_recipe_button);
        mClearButton = findViewById(R.id.clear_button);
        Button mCancelButton = findViewById(R.id.cancel_button);
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
                startActivityForResult(Intent.createChooser(intent, getString(R.string.complete_action)), RC_PHOTO_PICKER);
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
                for (Ingredient ingredient : mIngredients) {
                    mIngredientBlobList += ingredient.getIngredient();
                }

                boolean favoriteSelection = mFavoritesCheckBox.isChecked();
                ArrayList<String> missingData = new ArrayList<>();
                recipeTitle = mRecipeTitleEditText.getText().toString();
                if (recipeTitle.equals("")) {
                    missingData.add(getString(R.string.recipe_name));
                }
                try {
                    prepTime = Long.parseLong(mPrepTimeEditText.getText().toString());
                } catch (Exception e) {
                    missingData.add(getString(R.string.prep_time));
                }
                try {
                    cookTime = Long.parseLong(mCookTimeEditText.getText().toString());
                } catch (Exception e) {
                    missingData.add(getString(R.string.cook_time));
                }
                try {
                    servings = Long.parseLong(mServesEditText.getText().toString());
                } catch (Exception e) {
                    missingData.add(getString(R.string.serves));
                }
                if (missingData.size() > 0) {
                    String toastMessage = getString(R.string.add_edit_save_error_toast_message);
                    for (Object string : missingData) {
                        toastMessage += "\n" + string.toString();
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
                            mRecipeId,
                            mUserId,
                            mIngredientBlobList);
                    if (mTaskId.equals(MainActivity.NEW)) {
                        mRecipeDatabaseReference.push().setValue(recipe);
                        finish();
                    } else if (mTaskId.equals(MainActivity.EDIT)) {
                        DatabaseReference rf = mFirebaseDatabase.getReference().child(getString(R.string.users_path_segment) + mUserId + getString(R.string.recipes_path_segment) + mRecipe.getRecipeId());
                        rf.setValue(recipe);
                        Intent intent = new Intent(AddEditActivity.this, DetailActivity.class);
                        intent.putExtra(MainActivity.RECIPE, recipe);
                        intent.putParcelableArrayListExtra(INGREDIENTS, recipe.getIngredients());
                        intent.putParcelableArrayListExtra(DIRECTIONS, recipe.getDirections());
                        startActivity(intent);
                    }

                }
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(AddEditActivity.this)
                        .setTitle(getString(R.string.are_you_sure) + mRecipe.getTitle() + getString(R.string.question_mark))
                        .setPositiveButton(getString(R.string.affirmative), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        DatabaseReference rf = mFirebaseDatabase.getReference().child(getString(R.string.users_path_segment) + mUserId + getString(R.string.recipes_path_segment) + mRecipe.getRecipeId());
                                        rf.removeValue();
                                        Intent intent = new Intent(AddEditActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                        )
                        .setNegativeButton(getString(R.string.no_string), null)
                        .create();
                dialog.show();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            assert selectedImageUri != null;
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
                            throw Objects.requireNonNull(task.getException());
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
                            Toast.makeText(getApplicationContext(), R.string.photo_upload_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setPicture() {
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
