package com.fatmogul.recipebox;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseSetup extends Application {
    @Override
    public void onCreate() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        super.onCreate();
    }
}
