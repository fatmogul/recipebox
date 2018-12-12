package com.fatmogul.recipebox;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Recipe implements Parcelable {

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };
    private String title;
    private String titleLower;
    private long prepTime;
    private long cookTime;
    private long servings;
    private ArrayList<Ingredient> ingredients;
    private ArrayList<Direction> directions;
    private String photoUrl;
    private boolean favorite;
    private String recipeId;
    private String userId;
    private String ingredientListBlob;

    public Recipe() {
    }

    Recipe(String title, String titleLower, long prepTime, long cookTime, long servings, ArrayList<Ingredient> ingredients, ArrayList<Direction> directions, String photoUrl, boolean favorite, String recipeId, String userId, String ingredientListBlob) {
        this.title = title;
        this.titleLower = titleLower.toLowerCase();
        this.prepTime = prepTime;
        this.cookTime = cookTime;
        this.servings = servings;
        this.ingredients = ingredients;
        this.directions = directions;
        this.photoUrl = photoUrl;
        this.favorite = favorite;
        this.userId = userId;
        this.recipeId = recipeId;
        this.ingredientListBlob = ingredientListBlob;
    }

    private Recipe(Parcel in) {
        title = in.readString();
        titleLower = in.readString();
        prepTime = in.readLong();
        cookTime = in.readLong();
        servings = in.readLong();
        photoUrl = in.readString();
        favorite = in.readByte() != 0;
        recipeId = in.readString();
        userId = in.readString();
    }

    public String getTitle() {
        return title;
    }

    long getPrepTime() {
        return prepTime;
    }


    long getCookTime() {
        return cookTime;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<Direction> getDirections() {
        return directions;
    }

    public void setDirections(ArrayList<Direction> directions) {
        this.directions = directions;
    }

    String getPhotoUrl() {
        return photoUrl;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    long getServings() {
        return servings;
    }

    String getRecipeId() {
        return recipeId;
    }

    void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    String getUserId() {
        return userId;
    }

    void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(titleLower);
        dest.writeLong(prepTime);
        dest.writeLong(cookTime);
        dest.writeLong(servings);
        dest.writeString(photoUrl);
        dest.writeByte((byte) (favorite ? 1 : 0));
        dest.writeString(recipeId);
        dest.writeString(userId);
    }

    String getIngredientListBlob() {
        return ingredientListBlob;
    }

    void setIngredientListBlob(String ingredientListBlob) {
        this.ingredientListBlob = ingredientListBlob;
    }
}
