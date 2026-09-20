package org.micoli.coverwidgetcontainer.host

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View

object WidgetSnapshotRenderer {
    const val PAGE_BACKGROUND = 0xFF263238.toInt()

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
}
