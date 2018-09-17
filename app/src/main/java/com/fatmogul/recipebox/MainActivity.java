package com.fatmogul.recipebox;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static final int RC_SIGN_IN = 1;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mKeys = new ArrayList<>();
        final Spinner filterSpinner = findViewById(R.id.filter);
        final ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(this, R.array.food_filter, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);
        filterSpinner.setSelection(0, false);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mFilterSearch = filterSpinner.getItemAtPosition(position).toString();
                mAdapter.clear();
                detachDatabaseReadListener();
                attachDatabaseReadListener();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (savedInstanceState != null) {
            mUserId = savedInstanceState.getString("userId");
            mRecipes = savedInstanceState.getParcelableArrayList("recipes");
        }
        final Spinner favoritesSpinner = findViewById(R.id.favorites);
        ArrayAdapter<CharSequence> favoritesAdapter = ArrayAdapter.createFromResource(this, R.array.favorite_filter, android.R.layout.simple_spinner_item);
        favoritesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        favoritesSpinner.setAdapter(favoritesAdapter);
        favoritesSpinner.setSelection(0, false);
        favoritesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (favoritesSpinner.getItemAtPosition(position).toString().equals("Favorites")) {
                    mFavorites = true;
                } else {
                    mFavorites = false;
                }
                mAdapter.clear();
                detachDatabaseReadListener();
                attachDatabaseReadListener();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mRecipeListView = findViewById(R.id.recipeListView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = mFirebaseAuth.getInstance();
        if (mRecipes == null) {
            mRecipes = new ArrayList<>();
        }
        mAdapter = new RecipeAdapter(this, R.layout.recipe, mRecipes);
        mRecipeListView.setAdapter(mAdapter);


        final List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mUserId == null) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        onSignedInInitialize(user.getUid());

                    } else {
                        onSignedOutCleanup();
                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setIsSmartLockEnabled(false)
                                        .setAvailableProviders(providers)
                                        .build(),
                                RC_SIGN_IN);
                    }
                }
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("userId", mUserId);
        outState.putParcelableArrayList("recipes", mRecipes);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search_button).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setQueryHint("Find Recipe");

        final ImageView searchCloseButton = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchView.setQuery("", false);
                searchView.onActionViewCollapsed();
                menu.findItem(R.id.search_button).collapseActionView();
                mSearchQueryChanged = false;
                mSearchTerm = null;
                mAdapter.clear();
                detachDatabaseReadListener();
                attachDatabaseReadListener();

            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                if (mSearchQueryChanged == true) {
                    mSearchQueryChanged = false;
                    mAdapter.clear();
                    detachDatabaseReadListener();
                    attachDatabaseReadListener();
                    searchView.clearFocus();

                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    public void addRecipe(View view) {
        Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
        intent.putExtra("userId", mUserId);
        intent.putExtra("taskId", "new");
        startActivity(intent);
    }

    public void onSignedInInitialize(String userId) {
        mUserId = userId;
        attachDatabaseReadListener();
    }

    private void onSignedOutCleanup() {
        mUserId = null;
        mAdapter.clear();
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {

        if (mChildEventListener == null) {
            mRecipeDatabaseReference = mFirebaseDatabase.getReference().child("users/" + mUserId + "/recipes");
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                    AsyncTask task = new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] objects) {

                            boolean meetSearchCriteria = true;
                            Recipe thisRecipe = dataSnapshot.getValue(Recipe.class);
                            ArrayList<Ingredient> ingredientTempArray = new ArrayList<>();
                            DataSnapshot ingredientSnap = dataSnapshot.child("/ingredients");
                            Iterable<DataSnapshot> ingredientMatchSnapshot = ingredientSnap.getChildren();
                            for (DataSnapshot ingredient : ingredientMatchSnapshot) {
                                ingredientTempArray.add(ingredient.getValue(Ingredient.class));
                            }
                            thisRecipe.setIngredients(ingredientTempArray);

                            ArrayList<Direction> directionTempArray = new ArrayList<>();
                            DataSnapshot directionSnap = dataSnapshot.child("/directions");
                            Iterable<DataSnapshot> directionMatchSnapshot = directionSnap.getChildren();
                            for (DataSnapshot direction : directionMatchSnapshot) {
                                directionTempArray.add(direction.getValue(Direction.class));
                            }
                            thisRecipe.setDirections(directionTempArray);

                            thisRecipe.setRecipeId(dataSnapshot.getKey());
                            thisRecipe.setUserId(mUserId);
                            mRecipeDatabaseReference.child(dataSnapshot.getKey()).child("recipeId").setValue(dataSnapshot.getKey());
                            mRecipeDatabaseReference.child(dataSnapshot.getKey()).child("userId").setValue(mUserId);
                            if (mSearchTerm != null && !thisRecipe.getTitleLower().contains(mSearchTerm.toLowerCase())) {
                                meetSearchCriteria = false;
                            }
                            if (mFilterSearch != null && !mFilterSearch.equals("All Foods") && !thisRecipe.getTitleLower().contains(mFilterSearch.toLowerCase())) {
                                meetSearchCriteria = false;
                            }
                            if (mFavorites == true && !thisRecipe.isFavorite()) {
                                meetSearchCriteria = false;
                            }
                            if (meetSearchCriteria) {
                                mKeys.add(thisRecipe.getRecipeId());
                                mRecipes.add(thisRecipe);
                            }

                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            mAdapter.notifyDataSetChanged();
                            super.onPostExecute(o);
                        }
                    };
                task.execute();};

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    int index = mKeys.indexOf(dataSnapshot.getKey());
                    mRecipes.remove(mRecipes.get(index));
                    mRecipes.set(index,dataSnapshot.getValue(Recipe.class));
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    int index = mKeys.indexOf(dataSnapshot.getKey());
                    mRecipes.remove(mRecipes.get(index));
                    mAdapter.notifyDataSetChanged();
                    mKeys.remove(dataSnapshot.getKey());

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

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mKeys.clear();
            mRecipeDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }
}
