package com.fatmogul.recipebox;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class RecipeWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

private final Context mContext;
private final int appWidgetId;
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
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUserId = Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();
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

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row=new RemoteViews(mContext.getPackageName(),
                R.layout.recipe_widget);

        row.setTextViewText(R.id.widget_recipe_name_text_view, mRecipes.get(position).getTitle());

        Intent intent=new Intent();
        intent.putExtra("recipe", mRecipes.get(position));
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