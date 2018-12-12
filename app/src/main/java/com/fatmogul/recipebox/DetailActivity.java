package com.fatmogul.recipebox;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.fatmogul.recipebox.MainActivity.EDIT;
import static com.fatmogul.recipebox.MainActivity.FAVORITE;
import static com.fatmogul.recipebox.MainActivity.RECIPE;
import static com.fatmogul.recipebox.MainActivity.TASK_ID;
import static com.fatmogul.recipebox.MainActivity.USER_ID;

/*
    DetailActivity serves as the screen to view the greater details for the recipe, such as the
        directions and the ingredients.

    @mRecipe is the Recipe being viewed on the detail screen, obtained from the Intent which brings
        you to the screen
    @mIngredients is the ArrayList of INGREDIENTS received from the Intent which brings you to this
        screen
    @mDirections is the ArrayList of DIRECTIONS received from the Intent which brings you to this
        screen
    @mIngredientAdapter is the local custom IngredientAdapter for displaying the ingredients in this
        view
    @mDirectionAdapter is the local custom DirectionAdapter for displaying the directions in this
        view
    @mIngredientListView is the reference to the view which mIngredientAdapter populates
    @mDirectionListView is the reference to the view which mDirectionAdapter populates
    @mPrepTimeTextView is the TextView populated with the prepTime attribute of the Recipe
    @mCookTimeTextView is the TextView populated with the cookTime attribute of the Recipe
    @mServingsTextView is the TextView populated with the servings attribute of the Recipe
    @mImageView is the ImageView populated with the image at the photoDownloadUri attribute of the
        Recipe
    @mRecipeTitleView is the TextView populated with the title attribute of the Recipe
    @mPhotoDownloadUri is the local value for the photoDownloadUri attribute of the Recipe, made
        global for DetailActivity for cases of state change

    @mEditButton is the Edit Button displayed in the activity view, utilized for directing the
        application to the AddEditActivity activity
    @mShareButton is the Share Button displayed on the activity view, utilized for directing the
        application to an intent for sharing the Recipe through outside applications.
    @mFavoriteButton is the Favorite Button displayed in the activity view, populated by the
    favorite attribute of the Recipe.
 */
public class DetailActivity extends AppCompatActivity {

    private Recipe mRecipe;
    private ArrayList<Ingredient> mIngredients;
    private ArrayList<Direction> mDirections;
    private Button mFavoriteButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mRecipe = getIntent().getParcelableExtra(RECIPE);
        mIngredients = getIntent().getParcelableArrayListExtra(MainActivity.INGREDIENTS);
        mDirections = getIntent().getParcelableArrayListExtra(MainActivity.DIRECTIONS);
        setTitle(mRecipe.getTitle());

        ListView mIngredientListView = findViewById(R.id.ingredients_detail_list_view);
        IngredientAdapter mIngredientAdapter = new IngredientAdapter(this, R.layout.ingredient_display_list_view, mIngredients);
        mIngredientListView.setAdapter(mIngredientAdapter);

        ListView mDirectionListView = findViewById(R.id.directions_detail_list_view);
        DirectionAdapter mDirectionAdapter = new DirectionAdapter(this, R.layout.direction_display_list_view, mDirections);
        mDirectionListView.setAdapter(mDirectionAdapter);

        TextView mPrepTimeTextView = findViewById(R.id.prep_time_variable_text_view_detail_screen);
        TextView mCookTimeTextView = findViewById(R.id.cook_time_variable_text_view_detail_screen);
        TextView mServingsTextView = findViewById(R.id.servings_variable_detail_view);
        ImageView mImageView = findViewById(R.id.detail_image_view);
        TextView mRecipeTitleView = findViewById(R.id.recipe_title_detail_text_view);


        mPrepTimeTextView.setText(String.valueOf(mRecipe.getPrepTime()));
        mCookTimeTextView.setText(String.valueOf(mRecipe.getCookTime()));
        mServingsTextView.setText(String.valueOf(mRecipe.getServings()));
        mRecipeTitleView.setText(mRecipe.getTitle());
        /*
        Retrieving the image from Firebase Storage and placing it in the ImageView
         */
        try {
            Uri mPhotoDownloadUri = Uri.parse(mRecipe.getPhotoUrl());
            Picasso.get().load(mPhotoDownloadUri).fit().centerCrop().placeholder(R.drawable.ic_add_a_photo_grey_24dp).into(mImageView);
            mImageView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            mImageView.setVisibility(View.GONE);
        }

        Button mShareButton = findViewById(R.id.detail_share_button);

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
Share button currently shares a limited text version of the recipe
TODO: update share functionality to include image and html formatted text.
 */
                StringBuilder ingredientText = new StringBuilder();
                for (Ingredient ingredient : mIngredients) {
                    ingredientText.append("\n").append(ingredient.getQuantity()).append(" ").append(ingredient.getMeasurement()).append(" ").append(ingredient.getIngredient());
                }
                StringBuilder directionText = new StringBuilder();
                int positionCounter = 0;
                for (Direction direction : mDirections) {
                    positionCounter += 1;
                    directionText.append("\n").append(String.valueOf(positionCounter)).append(". ").append(direction.getDirectionText());
                }
                String tempText = mRecipe.getTitle() + "\n" +
                        String.format(getString(R.string.prep_time_share_string), Long.toString(mRecipe.getPrepTime())) + "\n" +
                        String.format(getString(R.string.cook_time_share_string), Long.toString(mRecipe.getCookTime())) + "\n" +
                        String.format(getString(R.string.serves_share_string), Long.toString(mRecipe.getServings())) + "\n" +
                        String.format(getString(R.string.ingredients_share_string), ingredientText.toString()) + "\n" +
                        String.format(getString(R.string.directions_share_string), directionText.toString()) + "\n";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_TEXT, tempText);
                Intent.createChooser(intent, getString(R.string.share_using));
                startActivity(intent);

            }
        });

        mFavoriteButton = findViewById(R.id.detail_favorite_button);
        if (mRecipe.isFavorite()) {
            mFavoriteButton.setText(R.string.unfavorite);
        } else {
            mFavoriteButton.setText(R.string.add_favorite);
        }
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFavorite = mRecipe.isFavorite();
                String toastText;
                if (isFavorite) {
                    isFavorite = false;
                    toastText = String.format(getString(R.string.recipe_unfavorited), mRecipe.getTitle());
                    mFavoriteButton.setText(R.string.add_favorite);
                } else {
                    isFavorite = true;
                    toastText = String.format(getString(R.string.recipe_favorited), mRecipe.getTitle());
                    mFavoriteButton.setText(R.string.unfavorite);
                }
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference rf = db.getReference().child(MainActivity.RF_USERS_PATH + mRecipe.getUserId() + MainActivity.RF_RECIPES_PATH);
                rf.child(mRecipe.getRecipeId()).child(FAVORITE).setValue(isFavorite);
                mRecipe.setFavorite(isFavorite);
                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
            }
        });

        Button mEditButton = findViewById(R.id.detail_edit_button);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, AddEditActivity.class);
                intent.putExtra(USER_ID, mRecipe.getUserId());
                intent.putExtra(TASK_ID, EDIT);
                intent.putExtra(RECIPE, mRecipe);
                intent.putParcelableArrayListExtra(MainActivity.INGREDIENTS, mIngredients);
                intent.putParcelableArrayListExtra(MainActivity.DIRECTIONS, mDirections);

                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
