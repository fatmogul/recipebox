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
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
/*
@mKeys is utilized to keep track of which recipe is in focus.  It is updated along with the database itself.
 */

    public final static String USER_ID = "userId";
    public final static String TASK_ID = "taskId";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRecipeDatabaseReference;
    private ChildEventListener mChildEventListener;
    private RecipeAdapter mAdapter;
    private ArrayList<String> mKeys;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String mUserId;
    private String mSearchTerm;
    private boolean mSearchQueryChanged = false;
    private String mFilterSearch;
    private boolean mFavorites;
    public final static String RECIPE = "recipe";
    public final static String INGREDIENTS = "ingredients";
    public final static String DIRECTIONS = "directions";
    public final static String PHOTO_URI = "photoUriString";
    public final static String RF_USERS_PATH = "users/";
    public final static String RF_RECIPES_PATH = "/recipes";
    public final static String FAVORITE = "favorite";
    public final static String EDIT = "edit";
    private static final int RC_SIGN_IN = 1;
    private final static String RECIPE_ID = "recipeId";
    private final static String RF_INGREDIENTS_PATH = "/ingredients";
    private final static String RF_DIRECTIONS_PATH = "/directions";
    private final static String NEW = "new";
    private ArrayList<Recipe> mRecipes;

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
                mRecipes.clear();
                detachDatabaseReadListener();
                attachDatabaseReadListener();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (savedInstanceState != null) {
            mUserId = savedInstanceState.getString(USER_ID);
            mRecipes = savedInstanceState.getParcelableArrayList(RECIPE);
        }
        final Spinner favoritesSpinner = findViewById(R.id.favorites);
        ArrayAdapter<CharSequence> favoritesAdapter = ArrayAdapter.createFromResource(this, R.array.favorite_filter, android.R.layout.simple_spinner_item);
        favoritesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        favoritesSpinner.setAdapter(favoritesAdapter);
        favoritesSpinner.setSelection(0, false);
        favoritesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mFavorites = favoritesSpinner.getItemAtPosition(position).toString().equals(FAVORITE);
                mRecipes.clear();
                detachDatabaseReadListener();
                attachDatabaseReadListener();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ListView mRecipeListView = findViewById(R.id.recipeListView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
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
        outState.putString(USER_ID, mUserId);
        outState.putParcelableArrayList(RECIPE, mRecipes);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search_button).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(Objects.requireNonNull(searchManager).getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setQueryHint(getString(R.string.find_recipe));

        final ImageView searchCloseButton = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchView.setQuery("", false);
                searchView.onActionViewCollapsed();
                menu.findItem(R.id.search_button).collapseActionView();
                mSearchQueryChanged = false;
                mSearchTerm = null;
                mRecipes.clear();
                detachDatabaseReadListener();
                attachDatabaseReadListener();

            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                if (mSearchQueryChanged) {
                    mSearchQueryChanged = false;
                    mRecipes.clear();
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
                Toast.makeText(this, R.string.signed_in, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.sign_in_canceled, Toast.LENGTH_SHORT).show();
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
        intent.putExtra(USER_ID, mUserId);
        intent.putExtra(TASK_ID, NEW);
        startActivity(intent);
    }

    private void onSignedInInitialize(String userId) {
        mUserId = userId;
        attachDatabaseReadListener();
    }

    private void onSignedOutCleanup() {
        mUserId = null;
        mRecipes.clear();
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {

        if (mChildEventListener == null) {
            mRecipeDatabaseReference = mFirebaseDatabase.getReference().child(RF_USERS_PATH + mUserId + RF_RECIPES_PATH);
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                    //TODO: create class for Asynctask to fix static field leak
                    AsyncTask task = new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] objects) {

                            boolean meetSearchCriteria = true;
                            Recipe thisRecipe = dataSnapshot.getValue(Recipe.class);
                            ArrayList<Ingredient> ingredientTempArray = new ArrayList<>();
                            DataSnapshot ingredientSnap = dataSnapshot.child(RF_INGREDIENTS_PATH);
                            Iterable<DataSnapshot> ingredientMatchSnapshot = ingredientSnap.getChildren();
                            for (DataSnapshot ingredient : ingredientMatchSnapshot) {
                                ingredientTempArray.add(ingredient.getValue(Ingredient.class));
                                thisRecipe.setIngredientListBlob(Objects.requireNonNull(thisRecipe).getIngredientListBlob() + Objects.requireNonNull(ingredient.getValue(Ingredient.class)).getIngredient().toLowerCase());
                            }
                            Objects.requireNonNull(thisRecipe).setIngredients(ingredientTempArray);

                            ArrayList<Direction> directionTempArray = new ArrayList<>();
                            DataSnapshot directionSnap = dataSnapshot.child(RF_DIRECTIONS_PATH);
                            Iterable<DataSnapshot> directionMatchSnapshot = directionSnap.getChildren();
                            for (DataSnapshot direction : directionMatchSnapshot) {
                                directionTempArray.add(direction.getValue(Direction.class));
                            }
                            thisRecipe.setDirections(directionTempArray);

                            thisRecipe.setRecipeId(dataSnapshot.getKey());
                            thisRecipe.setUserId(mUserId);
                            mRecipeDatabaseReference.child(Objects.requireNonNull(dataSnapshot.getKey())).child(RECIPE_ID).setValue(dataSnapshot.getKey());
                            mRecipeDatabaseReference.child(dataSnapshot.getKey()).child(USER_ID).setValue(mUserId);
                            if (mSearchTerm != null && !thisRecipe.getIngredientListBlob().contains(mSearchTerm.toLowerCase())) {
                                meetSearchCriteria = false;
                            }
                            if (mFilterSearch != null && !mFilterSearch.equals(getString(R.string.all_foods)) && !thisRecipe.getIngredientListBlob().contains(mFilterSearch.toLowerCase())) {
                                meetSearchCriteria = false;
                            }
                            if (mFavorites && !thisRecipe.isFavorite()) {
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
                            super.onPostExecute(o);

                            mAdapter.notifyDataSetChanged();
                            }
                    };
                task.execute();}

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    int index = mKeys.indexOf(dataSnapshot.getKey());
                    mRecipes.remove(mRecipes.get(index));
                    mKeys.remove(index);
                    mRecipes.add(index,dataSnapshot.getValue(Recipe.class));
                    mKeys.add(index,dataSnapshot.getKey());
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
