package org.micoli.coverwidgetcontainer.cover

import android.content.Context
import org.micoli.coverwidgetcontainer.host.HostedWidgetManager
import org.micoli.coverwidgetcontainer.host.PageLayout
import org.micoli.coverwidgetcontainer.host.ViewHitTest

object TapDispatcher {

    // Main thread only: hosted views are not attached, so the click is performed directly on the target view.
    fun dispatch(context: Context, appWidgetId: Int, column: Int, row: Int) {
        val geometry = ContainerRenderer.geometry(appWidgetId) ?: return
        val x = (column + 0.5f) / TapGrid.COLUMNS * geometry.widthPx
        val y = (row + 0.5f) / TapGrid.ROWS * geometry.heightPx
        val slotIndex = PageLayout.slotIndexAt(geometry.slots, x, y) ?: return
        val slot = geometry.slots[slotIndex]
        val hostedView = HostedWidgetManager.get(context).view(geometry.widgetIds[slotIndex]) ?: return
        ViewHitTest.findClickable(hostedView, x - slot.left, y - slot.top)?.performClick()
    }
}
