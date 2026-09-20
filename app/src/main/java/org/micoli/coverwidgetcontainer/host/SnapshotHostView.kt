package org.micoli.coverwidgetcontainer.host

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.os.Bundle
import android.util.SizeF
import android.widget.RemoteViews

class SnapshotHostView(context: Context) : AppWidgetHostView(context) {
    var onContentChanged: (() -> Unit)? = null
    private var lastSize: SizeF? = null

    override fun updateAppWidget(remoteViews: RemoteViews?) {
        super.updateAppWidget(remoteViews)
        onContentChanged?.invoke()
    }

    // Each size update makes the provider push new views, so only send real changes.
    fun applySize(widthDp: Float, heightDp: Float) {
        val size = SizeF(widthDp, heightDp)
        if (size == lastSize) return
        lastSize = size
        updateAppWidgetSize(Bundle(), listOf(size))
    }
}
