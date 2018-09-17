package com.fatmogul.recipebox;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RecipeWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

private Context mContext;
private int appWidgetId;
private ArrayList<Recipe> mRecipes;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRecipeDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private String mUserId;



    public RecipeWidgetRemoteViewsFactory(Context context, Intent intent) {
    this.mContext=context;
    appWidgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID);


    }

    @Override
    public void onCreate() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = mFirebaseAuth.getInstance();
        mUserId = mFirebaseAuth.getCurrentUser().getUid();
        mRecipeDatabaseReference = mFirebaseDatabase.getReference().child("users/" + mUserId + "/recipes");
        mRecipeDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mRecipes = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe thisRecipe = snapshot.getValue(Recipe.class);
                    mRecipes.add(thisRecipe);
                }
                AppWidgetManager.getInstance(mContext).notifyAppWidgetViewDataChanged(appWidgetId,R.id.recipeWidgetListView);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onDestroy() {
        // no-op
    }

    @Override
    public int getCount() {
        if(mRecipes != null){
        Log.d(String.valueOf(mRecipes.size()), "getCount: ");
        return(mRecipes.size());}
        else{
            return 0;
        }
    }
    //TODO: make sure data is validated appropriately
    //todo: make a giant text field of all fields for the search function to use
    //todo: navigation using a d-pad
    //todo: content descriptions
    //todo: strings in strings.xml
    //todo: ensure rtl
    //todo: make sure that an appbar is included
    //todo: set up the install release gradle task
    //todo: set up signing configuration
    
    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row=new RemoteViews(mContext.getPackageName(),
                R.layout.recipe_widget);

        row.setTextViewText(R.id.widget_recipe_name_text_view, mRecipes.get(position).getTitle());

        Intent intent=new Intent();
        intent.putExtra("recipe",mRecipes.get(position));
        intent.putParcelableArrayListExtra("ingredients",mRecipes.get(position).getIngredients());
        intent.putParcelableArrayListExtra("directions",mRecipes.get(position).getDirections());

        row.setOnClickFillInIntent(R.id.widget_recipe_name_text_view, intent);

        return(row);
    }

    @Override
    public RemoteViews getLoadingView() {
        return(null);
    }

    @Override
    public int getViewTypeCount() {
        return(1);
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    @Override
    public boolean hasStableIds() {
        return(true);
    }

    @Override
    public void onDataSetChanged() {
        // no-op
    }
}