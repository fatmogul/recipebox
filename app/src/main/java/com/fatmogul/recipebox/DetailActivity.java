package com.fatmogul.recipebox;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class DetailActivity extends AppCompatActivity {

private Recipe mRecipe;
private ArrayList<Ingredient> mIngredients;
private ArrayList<Direction> mDirections;
private IngredientAdapter mIngredientAdapter;
private DirectionAdapter mDirectionAdapter;
private ListView mIngredientListView;
private ListView mDirectionListView;
private TextView mPrepTimeTextView;
private TextView mCookTimeTextView;
private TextView mServingsTextView;
private ImageView mImageView;
private Uri mPhotoDownloadUri;

private Button mEditButton;
private Button mShareButton;
private Button mFavoriteButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mRecipe = getIntent().getParcelableExtra("recipe");
        mIngredients = getIntent().getParcelableArrayListExtra("ingredients");
        mDirections = getIntent().getParcelableArrayListExtra("directions");
        setTitle(mRecipe.getTitle());

        mIngredientListView = findViewById(R.id.ingredients_detail_list_view);
        mIngredientAdapter = new IngredientAdapter(this, R.layout.ingredient_display_list_view, mIngredients);
        mIngredientListView.setAdapter(mIngredientAdapter);

        mDirectionListView = findViewById(R.id.directions_detail_list_view);
        mDirectionAdapter = new DirectionAdapter(this,R.layout.direction_display_list_view,mDirections);
        mDirectionListView.setAdapter(mDirectionAdapter);

        mPrepTimeTextView = findViewById(R.id.prep_time_variable_text_view_detail_screen);
        mCookTimeTextView = findViewById(R.id.cook_time_variable_text_view_detail_screen);
        mServingsTextView = findViewById(R.id.servings_variable_detail_view);
        mImageView = findViewById(R.id.detail_image_view);


        mPrepTimeTextView.setText(String.valueOf(mRecipe.getPrepTime()));
        mCookTimeTextView.setText(String.valueOf(mRecipe.getCookTime()));
        mServingsTextView.setText(String.valueOf(mRecipe.getServings()));
        try{
        mPhotoDownloadUri = Uri.parse(mRecipe.getPhotoUrl());
            Picasso.get().load(mPhotoDownloadUri).fit().centerCrop().placeholder(R.drawable.ic_add_a_photo_grey_24dp).into(mImageView);
            mImageView.setVisibility(View.VISIBLE);
        } catch (Exception e){
            mImageView.setVisibility(View.GONE);
        }

        mShareButton = findViewById(R.id.detail_share_button);

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempText = mRecipe.getTitle() +
                        "\nPrep Time: " + mRecipe.getPrepTime() +
                        "\nCook Time: " + mRecipe.getCookTime() +
                        "\nServes: " + mRecipe.getServings() +
                        "\nIngredients: " + mRecipe.getIngredients() +
                        "\nDirections: " + mRecipe.getDirections();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_TEXT,tempText);
                Intent.createChooser(intent,"Share using");
                startActivity(intent);

            }
        });

        mFavoriteButton = findViewById(R.id.detail_favorite_button);
        if(mRecipe.isFavorite()){
            mFavoriteButton.setText("Remove from Favorites");
        }else{
            mFavoriteButton.setText("Add to Favorites");
        }
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFavorite = mRecipe.isFavorite();
                String toastText;
                if(isFavorite){
                    isFavorite = false;
                    toastText = mRecipe.getTitle() + " unfavorited.";
                    mFavoriteButton.setText("Add to Favorites");
                }else{
                    isFavorite = true;
                    toastText = mRecipe.getTitle() + " favorited.";
                    mFavoriteButton.setText("Remove from Favorites");
                }
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference rf = db.getReference().child("users/" + mRecipe.getUserId() + "/recipes");
                rf.child(mRecipe.getRecipeId()).child("favorite").setValue(isFavorite);
                mRecipe.setFavorite(isFavorite);
                Toast.makeText(getApplicationContext(),toastText,Toast.LENGTH_SHORT).show();
            }
        });

        mEditButton = findViewById(R.id.detail_edit_button);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this,AddEditActivity.class);
                intent.putExtra("userId",mRecipe.getUserId());
                intent.putExtra("taskId","edit");
                startActivity(intent);
            }
        });
    }
}
