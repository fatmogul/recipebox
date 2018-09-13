package com.fatmogul.recipebox;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Recipe implements Parcelable{

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

    public Recipe() {
    }

    public Recipe(String title, String titleLower, long prepTime, long cookTime, long servings, ArrayList<Ingredient> ingredients, ArrayList<Direction> directions, String photoUrl, boolean favorite, String recipeId, String userId){
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
    }

    protected Recipe(Parcel in) {
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

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public long getPrepTime() {
        return prepTime;
    }
    public void setPrepTime(long prepTime) {
        this.prepTime = prepTime;
    }


    public long getCookTime() {
        return cookTime;
    }

    public void setCookTime(long cookTime) {
        this.cookTime = cookTime;
    }

    public List getIngredients() {
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getTitleLower() {
        return titleLower;
    }

    public void setTitleLower(String titleLower) {
        this.titleLower = titleLower.toLowerCase();
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public long getServings() {
        return servings;
    }

    public void setServings(long servings) {
        this.servings = servings;
    }

    public String getRecipeId(){return recipeId;}

    public void setRecipeId(String recipeId){this.recipeId = recipeId;}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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
}
