package org.micoli.coverwidgetcontainer.cover

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.micoli.coverwidgetcontainer.data.ContainerIndex

abstract class ContainerWidgetProvider : AppWidgetProvider() {

    override fun onDisabled(context: Context) {
        if (ContainerWidgetUpdater.isAnyPlaced(context)) return
        OverlayPermissionNotification.cancel(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val index = ContainerIndex.fromClassName(javaClass.name) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                appWidgetIds.forEach { ContainerRenderer.update(context, it, index) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
