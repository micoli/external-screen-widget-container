package org.micoli.coverwidgetcontainer.host

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import kotlin.math.sqrt

object WidgetSnapshotRenderer {
    const val PAGE_BACKGROUND = 0xFF263238.toInt()
    private const val MAX_PIXELS = 1_000_000

    fun renderWidget(
        view: SnapshotHostView,
        width: Int,
        height: Int,
        density: Float,
        background: Int = Color.TRANSPARENT,
        resizeWidget: Boolean = false,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(background)
        if (resizeWidget) view.applySize(width / density, height / density)
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, width, height)
        view.draw(canvas)
        return bitmap
    }

    fun renderPage(
        manager: HostedWidgetManager,
        appWidgetIds: List<Int>,
        slots: List<SlotBounds>,
        width: Int,
        height: Int,
        density: Float,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        appWidgetIds.forEachIndexed { position, appWidgetId ->
            val view = manager.view(appWidgetId) ?: return@forEachIndexed
            val slot = slots[position]
            val rendered = renderWidget(view, slot.width, slot.height, density, resizeWidget = true)
            canvas.drawBitmap(rendered, slot.left.toFloat(), slot.top.toFloat(), null)
            rendered.recycle()
        }
        return bitmap
    }

    fun fitToBudget(widthPx: Int, heightPx: Int): Pair<Int, Int> {
        val pixels = widthPx.toLong() * heightPx
        if (pixels <= MAX_PIXELS) return widthPx to heightPx
        val scale = sqrt(MAX_PIXELS.toDouble() / pixels)
        return (widthPx * scale).toInt() to (heightPx * scale).toInt()
    }
}
