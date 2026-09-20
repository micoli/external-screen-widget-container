package org.micoli.coverwidgetcontainer.cover

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.micoli.coverwidgetcontainer.data.ContainerIndex
import org.micoli.coverwidgetcontainer.host.PageNavigation

abstract class ContainerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val index = ContainerIndex.fromClassName(javaClass.name) ?: return
        SnapshotService.start(context)
        runAsync { appWidgetIds.forEach { ContainerRenderer.update(context, it, index, force = true) } }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        val index = ContainerIndex.fromClassName(javaClass.name) ?: return
        runAsync { ContainerRenderer.update(context, appWidgetId, index, force = true) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != PageNavigation.ACTION_TURN_PAGE) {
            super.onReceive(context, intent)
            return
        }
        val index = ContainerIndex.fromClassName(javaClass.name) ?: return
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return
        val delta = intent.getIntExtra(PageNavigation.EXTRA_DELTA, 0)
        runAsync { ContainerRenderer.turnPage(context, appWidgetId, index, delta) }
    }

    private fun runAsync(block: suspend () -> Unit) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                block()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
