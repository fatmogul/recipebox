package com.fatmogul.recipebox;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/*
This is utilized to ensure we are only getting an instance of Firebase once, primarily due to issues with setting up the widget.
 */
public class FirebaseSetup extends Application {
    @Override
    public void onCreate() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        super.onCreate();
    }
}
