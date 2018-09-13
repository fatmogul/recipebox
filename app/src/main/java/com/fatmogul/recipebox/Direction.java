package com.fatmogul.recipebox;

import android.os.Parcel;
import android.os.Parcelable;

public class Direction implements Parcelable {
    private String directionText;

    public Direction(){}

    public Direction(String directionText) {
        this.directionText = directionText;
    }

    protected Direction(Parcel in) {
        directionText = in.readString();
    }

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

    public String getDirectionText() {
        return directionText;
    }

    public void setDirectionText(String directionText) {
        this.directionText = directionText;
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
