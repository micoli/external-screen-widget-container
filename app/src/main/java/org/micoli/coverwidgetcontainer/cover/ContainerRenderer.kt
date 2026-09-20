package org.micoli.coverwidgetcontainer.cover

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import org.micoli.coverwidgetcontainer.R
import org.micoli.coverwidgetcontainer.data.Container
import org.micoli.coverwidgetcontainer.data.ContainerRepository
import org.micoli.coverwidgetcontainer.overlay.OverlayState
import org.micoli.coverwidgetcontainer.overlay.WidgetMarker

// The cover widget draws nothing itself: the accessibility overlay finds it by its marker and covers it.
object ContainerRenderer {

    suspend fun update(context: Context, appWidgetId: Int, containerIndex: Int) {
        val appContext = context.applicationContext
        val container = ContainerRepository(appContext).get(containerIndex)
        val views = RemoteViews(appContext.packageName, R.layout.widget_container)
        views.setContentDescription(R.id.widget_root, WidgetMarker.contentDescription(container.index))
        if (OverlayState.isEnabled(appContext)) {
            views.setViewVisibility(R.id.widget_placeholder, View.GONE)
        } else {
            showServiceNeeded(appContext, views, container)
        }
        AppWidgetManager.getInstance(appContext).updateAppWidget(appWidgetId, views)
    }

    private fun showServiceNeeded(context: Context, views: RemoteViews, container: Container) {
        views.setViewVisibility(R.id.widget_placeholder, View.VISIBLE)
        views.setTextViewText(R.id.widget_title, container.name)
        views.setTextViewText(R.id.widget_subtitle, context.getString(R.string.widget_needs_service))
    }
}
