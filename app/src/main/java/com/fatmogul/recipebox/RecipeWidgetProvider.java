package com.fatmogul.recipebox;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Implementation of App Widget functionality.
 */
public class RecipeWidgetProvider extends AppWidgetProvider {


    @Override
    public void onUpdate(final Context ctxt, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        for (int i = 0; i < appWidgetIds.length; i++) {
                Intent svcIntent = new Intent(ctxt, WidgetService.class);
                Log.d("first", "onUpdate: ");
                svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
                svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
                RemoteViews widget = new RemoteViews(ctxt.getPackageName(),
                        R.layout.recipe_widget_provider);
                widget.setRemoteAdapter(R.id.recipeWidgetListView,
                        svcIntent);

                Intent clickIntent = new Intent(ctxt, DetailActivity.class);
                PendingIntent clickPI = PendingIntent
                        .getActivity(ctxt, 0,
                                clickIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                widget.setPendingIntentTemplate(R.id.recipeWidgetListView, clickPI);

                appWidgetManager.updateAppWidget(appWidgetIds[i], widget);

            }


        super.onUpdate(ctxt, appWidgetManager, appWidgetIds);
    }}
