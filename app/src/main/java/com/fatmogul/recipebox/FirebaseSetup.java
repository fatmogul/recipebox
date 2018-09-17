package com.fatmogul.recipebox;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseSetup extends Application {
    @Override
    public void onCreate() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        super.onCreate();
    }
}
