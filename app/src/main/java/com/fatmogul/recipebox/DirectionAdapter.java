package com.fatmogul.recipebox;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class DirectionAdapter extends ArrayAdapter<Direction> {

    public DirectionAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Direction> objects) {
        super(context, resource, objects);
    }
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.direction_display_list_view, parent, false);
        }

        ImageView removeButton = convertView.findViewById(R.id.remove_direction_button);
        final TextView directionLabelTextView = convertView.findViewById(R.id.direction_number_label);
        final TextView directionEditText = convertView.findViewById(R.id.direction_text_edit_text);
        ImageView editButton = convertView.findViewById(R.id.edit_direction_button);

        final Direction thisDirection = getItem(position);
        directionLabelTextView.setText(String.valueOf(position + 1));
        directionEditText.setText(thisDirection.getDirectionText());

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddEditActivity.updateDirection(position, (Activity) getContext());
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddEditActivity.removeDirection(position);
            }
        });
        if (directionEditText.getText().toString().equals(getContext().getResources().getString(R.string.none_loaded))) {
            directionLabelTextView.setVisibility(View.GONE);
            removeButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
            directionEditText.setVisibility(View.GONE);
        } else {
            directionLabelTextView.setVisibility(View.VISIBLE);
            removeButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
            directionEditText.setVisibility(View.VISIBLE);
        }


        return convertView;
    }
}
