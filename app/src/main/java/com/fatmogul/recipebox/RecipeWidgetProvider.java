package com.fatmogul.recipebox;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class RecipeWidgetProvider extends AppWidgetProvider {


    @Override
    public void onUpdate(final Context ctxt, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        for (int appWidgetId : appWidgetIds) {
            Intent svcIntent = new Intent(ctxt, WidgetService.class);
            Log.d("first", "onUpdate: ");
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
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

            appWidgetManager.updateAppWidget(appWidgetId, widget);

        }


        super.onUpdate(ctxt, appWidgetManager, appWidgetIds);
    }}
