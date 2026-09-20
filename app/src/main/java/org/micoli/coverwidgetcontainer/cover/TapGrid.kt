package org.micoli.coverwidgetcontainer.cover

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import java.util.concurrent.ConcurrentHashMap
import org.micoli.coverwidgetcontainer.R

// A RemoteViews click carries no coordinates, so the widget area is covered by a grid of clickable cells.
object TapGrid {
    const val COLUMNS = 8
    const val ROWS = 8
    const val EXTRA_APPWIDGET_ID = "tap_appwidget_id"
    const val EXTRA_COLUMN = "tap_column"
    const val EXTRA_ROW = "tap_row"

    private val pendingIntents = ConcurrentHashMap<String, PendingIntent>()

    fun install(context: Context, views: RemoteViews, appWidgetId: Int) {
        views.removeAllViews(R.id.widget_grid)
        repeat(ROWS) { row ->
            val rowViews = RemoteViews(context.packageName, R.layout.widget_tap_row)
            repeat(COLUMNS) { column ->
                val cell = RemoteViews(context.packageName, R.layout.widget_tap_cell)
                cell.setOnClickPendingIntent(R.id.widget_tap_cell, pendingIntent(context, appWidgetId, column, row))
                rowViews.addView(R.id.widget_tap_row, cell)
            }
            views.addView(R.id.widget_grid, rowViews)
        }
    }

    fun clear(views: RemoteViews) = views.removeAllViews(R.id.widget_grid)

    private fun pendingIntent(context: Context, appWidgetId: Int, column: Int, row: Int): PendingIntent =
        pendingIntents.getOrPut("$appWidgetId/$column/$row") {
            val intent = Intent(context, TapForwardActivity::class.java)
                .setData(Uri.parse("cover-tap://$appWidgetId/$column/$row"))
                .putExtra(EXTRA_APPWIDGET_ID, appWidgetId)
                .putExtra(EXTRA_COLUMN, column)
                .putExtra(EXTRA_ROW, row)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            val options = ActivityOptions.makeBasic()
                .setPendingIntentCreatorBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
            PendingIntent.getActivity(
                context.applicationContext,
                appWidgetId * COLUMNS * ROWS + row * COLUMNS + column,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                options.toBundle(),
            )
        }
}
