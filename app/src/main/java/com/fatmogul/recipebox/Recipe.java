package com.fatmogul.recipebox;

import java.util.List;

public class Recipe {

    private String title;
    private String titleLower;
    private long prepTime;
    private long cookTime;
    private long servings;
    private List ingredients;
    private List directions;
    private String photoUrl;
    private boolean favorite;
    private String recipeId;
    private String userId;

    public Recipe() {
    }

    public Recipe(String title, String titleLower, long prepTime, long cookTime, long servings, List ingredients, List directions, String photoUrl, boolean favorite, String recipeId, String userId){
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

    public void setIngredients(List ingredients) {
        this.ingredients = ingredients;
    }

    public List getDirections() {
        return directions;
    }

    public void setDirections(List directions) {
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
}
