package org.micoli.coverwidgetcontainer.host

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import org.micoli.coverwidgetcontainer.data.ContainerIndex

object PageNavigation {
    const val ACTION_TURN_PAGE = "org.micoli.coverwidgetcontainer.action.TURN_PAGE"
    const val EXTRA_DELTA = "delta"

    fun wrap(current: Int, delta: Int, pageCount: Int): Int {
        if (pageCount <= 0) return 0
        return Math.floorMod(current + delta, pageCount)
    }

    fun pendingIntent(context: Context, containerIndex: Int, appWidgetId: Int, delta: Int): PendingIntent {
        val provider = ComponentName(context.packageName, ContainerIndex.providerClassName(context.packageName, containerIndex))
        val intent = Intent(ACTION_TURN_PAGE)
            .setComponent(provider)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            .putExtra(EXTRA_DELTA, delta)
        return PendingIntent.getBroadcast(
            context,
            appWidgetId * 2 + if (delta > 0) 1 else 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
