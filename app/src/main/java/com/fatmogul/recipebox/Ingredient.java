package com.fatmogul.recipebox;

public class Ingredient {

    private long quantity;
    private String measurement;
    private String ingredient;

    public Ingredient(){}

    public Ingredient(long quantity, String measurement, String ingredient) {
        this.quantity = quantity;
        this.measurement = measurement;
        this.ingredient = ingredient;
    }


    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }
}
