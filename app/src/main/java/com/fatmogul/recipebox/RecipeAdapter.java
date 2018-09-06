package com.fatmogul.recipebox;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeAdapter extends ArrayAdapter<Recipe> {

    public RecipeAdapter(Context context, int resource, List<Recipe> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.recipe, parent, false);
        }

        ImageView recipeImageView = convertView.findViewById(R.id.recipeImageView);
        TextView titleTextView = convertView.findViewById(R.id.recipeNameView);
        TextView prepTimeTextView = convertView.findViewById(R.id.prepTimeView);
        TextView cookTimeTextView = convertView.findViewById(R.id.cookTimeView);
        TextView servingsView = convertView.findViewById(R.id.servingsView);

        Recipe recipe = getItem(position);

        if(recipe.getPhotoUrl() != null){
            recipeImageView.setVisibility(View.VISIBLE);
            Picasso.get().load(Uri.parse(recipe.getPhotoUrl())).fit().centerCrop().into(recipeImageView);
        }
        else{
            recipeImageView.setVisibility(View.GONE);
        }
        titleTextView.setText(recipe.getTitle());
        prepTimeTextView.setText(Long.toString(recipe.getPrepTime()));
        cookTimeTextView.setText(Long.toString(recipe.getCookTime()));
        servingsView.setText(Long.toString(recipe.getServings()));

        return convertView;
    }
}

