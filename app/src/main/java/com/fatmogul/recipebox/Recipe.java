package com.fatmogul.recipebox;

import java.util.List;

public class Recipe {

    private String title;
    private String titleLower;
    private String name;
    private long prepTime;
    private long cookTime;
    private List ingredients;
    private List directions;
    private String photoUrl;
    private boolean favorite;

    public Recipe() {
    }

    public Recipe(String title, String titleLower, String name, long prepTime, long cookTime, List ingredients, List directions, String photoUrl, boolean favorite){
        this.title = title;
        this.titleLower = titleLower.toLowerCase();
        this.name = name;
        this.prepTime = prepTime;
        this.cookTime = cookTime;
        this.ingredients = ingredients;
        this.directions = directions;
        this.photoUrl = photoUrl;
        this.favorite = favorite;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
