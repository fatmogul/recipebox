package com.fatmogul.recipebox;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchableActivity extends AppCompatActivity {
    public static final int RC_SIGN_IN = 1;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRecipeDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ListView mRecipeListView;
    private RecipeAdapter mAdapter;

    private FirebaseAuth mFirebaseAuth;
    private Query mQuery;

    private String mUserId;
    private String mQueryText;
    private String mSearchTerm;
    private boolean mSearchQueryChanged = false;



        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


            mRecipeListView = findViewById(R.id.recipeListView);

            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mFirebaseAuth = mFirebaseAuth.getInstance();

            List<Recipe> recipes = new ArrayList<>();
            mAdapter = new RecipeAdapter(this, R.layout.recipe, recipes);
            mRecipeListView.setAdapter(mAdapter);
            Intent intent = getIntent();
            mQueryText = intent.getStringExtra("query");
            setTitle(String.valueOf(mQueryText.charAt(0)).toUpperCase() + mQueryText.subSequence(1,mQueryText.length()) + " Recipes");
            mUserId = intent.getStringExtra("userId");
            if(mQueryText != null){
                mRecipeDatabaseReference = mFirebaseDatabase.getReference().child("users/" + mUserId + "/recipes");

                attachDatabaseReadListener();


            }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main,menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_button).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setQueryHint("Find Recipe");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                if(mSearchQueryChanged == true){
                    mSearchQueryChanged = false;
                    Intent intent = new Intent(SearchableActivity.this, SearchableActivity.class);
                    intent.putExtra("query",mSearchTerm);
                    intent.putExtra("userId",mUserId);
                    overridePendingTransition(0, 0);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(intent);}
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
                if (mSearchTerm == null && newFilter == null) {
                    return true;
                }
                if (mSearchTerm != null && mSearchTerm.equals(newFilter)) {
                    return true;
                }
                mSearchTerm = newFilter;
                mSearchQueryChanged = true;
                return true;
            }
        });


        return true;
    }



    public void addRecipe(View view) {
        Intent intent = new Intent(SearchableActivity.this, AddEditActivity.class);
        intent.putExtra("userId",mUserId);
        startActivity(intent);
    }
    public void onSignedInInitialize(String userId) {
        mUserId = userId;
        mRecipeDatabaseReference = mFirebaseDatabase.getReference().child("users/" + mUserId + "/recipes");
        attachDatabaseReadListener();
    }

    private void onSignedOutCleanup() {
        mUserId = null;
        mAdapter.clear();
        detachDatabaseReadListener();
    }
    private void attachDatabaseReadListener() {
        if(mChildEventListener == null) {

            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Recipe thisRecipe = dataSnapshot.getValue(Recipe.class);
                    if(thisRecipe.getTitleLower().contains(mQueryText.toLowerCase())){
                    mAdapter.add(thisRecipe);
                }}

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
            mRecipeDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener(){
        if(mChildEventListener != null) {
            mRecipeDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }
    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}



