package org.micoli.coverwidgetcontainer.overlay

import android.content.Context
import android.view.View
import android.view.ViewGroup
import org.micoli.coverwidgetcontainer.data.GridCell
import org.micoli.coverwidgetcontainer.data.PageGrid

// Lays each child out on the 2x2 page grid, according to the cell it was added with.
class GridPageLayout(context: Context, private val gapPx: Int) : ViewGroup(context) {
    private val cells = mutableMapOf<View, GridCell>()

    fun addCell(view: View, cell: GridCell) {
        cells[view] = cell
        addView(view)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
        cells.forEach { (view, cell) ->
            val bounds = PageGrid.cellBounds(cell, width, height, gapPx)
            view.measure(
                MeasureSpec.makeMeasureSpec(bounds.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(bounds.height, MeasureSpec.EXACTLY),
            )
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        cells.forEach { (view, cell) ->
            val bounds = PageGrid.cellBounds(cell, right - left, bottom - top, gapPx)
            view.layout(bounds.left, bounds.top, bounds.right, bounds.bottom)
        }
    }
}
