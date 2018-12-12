package com.fatmogul.recipebox;

import android.os.Parcel;
import android.os.Parcelable;

/*
Custom class for directions to the recipe.
 */
public class Direction implements Parcelable {
    public static final Creator<Direction> CREATOR = new Creator<Direction>() {
        @Override
        public Direction createFromParcel(Parcel in) {
            return new Direction(in);
        }

        @Override
        public Direction[] newArray(int size) {
            return new Direction[size];
        }
    };
    private String directionText;

    public Direction() {
    }

    Direction(String directionText) {
        this.directionText = directionText;
    }

    private Direction(Parcel in) {
        directionText = in.readString();
    }

    String getDirectionText() {
        return directionText;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(directionText);

    }
}
