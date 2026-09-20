package org.micoli.coverwidgetcontainer.cover

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import org.micoli.coverwidgetcontainer.data.ContainerIndex

object ContainerWidgetUpdater {

    fun isPlaced(context: Context, index: Int): Boolean =
        placedIds(context, componentFor(context, index)).isNotEmpty()

    fun isAnyPlaced(context: Context): Boolean =
        (1..ContainerIndex.COUNT).any { isPlaced(context, it) }

    fun refresh(context: Context, index: Int) {
        val component = componentFor(context, index)
        val ids = placedIds(context, component)
        if (ids.isEmpty()) return
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            .setComponent(component)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }

    fun refreshAll(context: Context) {
        (1..ContainerIndex.COUNT).forEach { refresh(context, it) }
    }

    private fun placedIds(context: Context, component: ComponentName): IntArray =
        AppWidgetManager.getInstance(context).getAppWidgetIds(component)

    fun componentFor(context: Context, index: Int) =
        ComponentName(context.packageName, ContainerIndex.providerClassName(context.packageName, index))
}
