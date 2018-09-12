package com.fatmogul.recipebox;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class IngredientAdapter extends ArrayAdapter<Ingredient> {


    public IngredientAdapter(@NonNull Context context, int resource, ArrayList<Ingredient> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.ingredient_display_list_view, parent, false);
        }

        ImageView removeButton = convertView.findViewById(R.id.remove_ingredient_button);
        final TextView quantityEditText = convertView.findViewById(R.id.quantity_edit_text);
        //TODO: Probably want to change the measurement field into a spinner of selection options, right?
        final TextView measurementEditText = convertView.findViewById(R.id.measurement_edit_text);
        final TextView ingredientEditText = convertView.findViewById(R.id.ingredient_name_edit_text);
        ImageView editButton = convertView.findViewById(R.id.edit_ingredient_button);

        final Ingredient thisIngredient = getItem(position);
        String quantityString = Long.toString(thisIngredient.getQuantity());


        quantityEditText.setText(quantityString);
        measurementEditText.setText(thisIngredient.getMeasurement());
        ingredientEditText.setText(thisIngredient.getIngredient());

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddEditActivity.updateIngredient(position, (Activity) getContext());
            }
        });
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddEditActivity.removeIngredient(position);
            }
        });
        if (ingredientEditText.getText().toString().equals(getContext().getResources().getString(R.string.none_loaded))) {
            removeButton.setVisibility(View.GONE);
            quantityEditText.setVisibility(View.GONE);
            measurementEditText.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
            ingredientEditText.setVisibility(View.VISIBLE);
        } else {
            removeButton.setVisibility(View.VISIBLE);
            quantityEditText.setVisibility(View.VISIBLE);
            measurementEditText.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
            ingredientEditText.setVisibility(View.VISIBLE);
        }


        return convertView;
    }
}
