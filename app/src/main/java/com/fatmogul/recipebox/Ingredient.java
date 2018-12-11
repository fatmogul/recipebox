package com.fatmogul.recipebox;

import android.os.Parcel;
import android.os.Parcelable;

/*
Custom class for creating a parcelable item of ingredients for use within the recipe.
 */
public class Ingredient implements Parcelable {

    private long quantity;
    private String measurement;
    private String ingredient;

    public Ingredient(){}

    /**
     * @param quantity    This parameter is utilized to house the number of measurement of the ingredient
     * @param measurement This parameter is utilized to house the measurement for the ingredient
     * @param ingredient  This parameter houses the name of the ingredient
     */
    Ingredient(long quantity, String measurement, String ingredient) {
        this.quantity = quantity;
        this.measurement = measurement;
        this.ingredient = ingredient;
    }


    private Ingredient(Parcel in) {
        quantity = in.readLong();
        measurement = in.readString();
        ingredient = in.readString();
    }

    public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
        @Override
        public Ingredient createFromParcel(Parcel in) {
            return new Ingredient(in);
        }

        @Override
        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };

    long getQuantity() {
        return quantity;
    }

    String getMeasurement() {
        return measurement;
    }

    String getIngredient() {
        return ingredient;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(quantity);
        dest.writeString(measurement);
        dest.writeString(ingredient);

    }
}
