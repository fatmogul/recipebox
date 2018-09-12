package com.fatmogul.recipebox;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends ArrayAdapter<Recipe> {
    
    public RecipeAdapter(Context context, int resource, ArrayList<Recipe> objects) {
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.recipe, parent, false);
        }
        
        final ImageView recipeImageView = convertView.findViewById(R.id.recipeImageView);
        TextView titleTextView = convertView.findViewById(R.id.recipeNameView);
        TextView prepTimeTextView = convertView.findViewById(R.id.prepTimeView);
        TextView cookTimeTextView = convertView.findViewById(R.id.cookTimeView);
        TextView servingsView = convertView.findViewById(R.id.servingsView);

        final Recipe recipe = getItem(position);

        if(recipe.getPhotoUrl() != null){
            recipeImageView.setVisibility(View.VISIBLE);
            Picasso.get().load(Uri.parse(recipe.getPhotoUrl())).fit().centerCrop().into(recipeImageView);
        }
        else{
            recipeImageView.setVisibility(View.GONE);
        }
        titleTextView.setText(recipe.getTitle());
        prepTimeTextView.setText(Long.toString(recipe.getPrepTime()) + " minutes");
        cookTimeTextView.setText(Long.toString(recipe.getCookTime()) + " minutes");
        servingsView.setText(Long.toString(recipe.getServings()));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),DetailActivity.class);
                getContext().startActivity(intent);
            }
        });

        ImageView shareIcon = convertView.findViewById(R.id.share_icon);
        shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempText = recipe.getTitle() +
                        "\nPrep Time: " + recipe.getPrepTime() +
                        "\nCook Time: " + recipe.getCookTime() +
                        "\nServes: " + recipe.getServings() +
                        "\nIngredients: " + recipe.getIngredients() +
                        "\nDirections: " + recipe.getDirections();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_TEXT,tempText);
                Intent.createChooser(intent,"Share using");
                getContext().startActivity(intent);
                 }
        });

        final ImageView favoriteIcon = convertView.findViewById(R.id.favorite_icon);
        if(recipe.isFavorite()){
            favoriteIcon.setImageResource(R.drawable.ic_favorite_red_24dp);
        }
        else{
            favoriteIcon.setImageResource(R.drawable.ic_favorite_border_grey_24dp);
        }
        favoriteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFavorite;
                String toastText;
                if (recipe.isFavorite()) {
                    favoriteIcon.setImageResource(R.drawable.ic_favorite_border_grey_24dp);
                    isFavorite = false;
                    toastText = recipe.getTitle() + " unfavorited.";

                } else {
                    favoriteIcon.setImageResource(R.drawable.ic_favorite_red_24dp);
                    isFavorite = true;
                    toastText = recipe.getTitle() + " favorited.";
                }

                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference rf = db.getReference().child("users/" + recipe.getUserId() + "/recipes");
                rf.child(recipe.getRecipeId()).child("favorite").setValue(isFavorite);
                recipe.setFavorite(isFavorite);
                Toast.makeText(getContext(),toastText,Toast.LENGTH_SHORT).show();
            }});
        return convertView;
    }
}

