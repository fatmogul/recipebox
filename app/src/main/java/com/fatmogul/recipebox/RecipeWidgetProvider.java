package com.fatmogul.recipebox;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class RecipeWidgetProvider extends AppWidgetProvider {

    ArrayList<Recipe> mRecipes;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRecipeDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ListView mRecipeListView;
    private RecipeAdapter mAdapter;
    private ArrayList<String> mKeys;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String mUserId;
    private String mSearchTerm;
    private boolean mSearchQueryChanged = false;
    private String mFilterSearch;
    private boolean mFavorites;
    void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,
                         final int appWidgetId) {

        // Construct the RemoteViews object
        Intent intent = new Intent(context, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recipe_widget_provider);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = mFirebaseAuth.getInstance();
            mRecipes = new ArrayList<>();

        mUserId = mFirebaseAuth.getCurrentUser().getUid();
        mRecipeDatabaseReference = mFirebaseDatabase.getReference().child("users/" + mUserId + "/recipes");
        mRecipeDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recipe_widget_provider);
                for( DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Recipe thisRecipe = snapshot.getValue(Recipe.class);
                    mRecipes.add(thisRecipe);
                    // Instruct the widget manager to update the widget
                }
                for(Recipe recipe : mRecipes) {
                    RemoteViews thisRecipeLayout = new RemoteViews(context.getPackageName(), R.layout.recipe_widget);
                    thisRecipeLayout.setTextViewText(R.id.widget_recipe_name_text_view, recipe.getTitle());
                    views.addView(R.id.recipeWidgetListView, thisRecipeLayout);
                    views.setOnClickPendingIntent(R.id.recipeWidgetListView, pendingIntent);
                }
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        views.setOnClickPendingIntent(R.id.recipeWidgetListView,pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);



    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

