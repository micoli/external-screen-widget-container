package org.micoli.coverwidgetcontainer.overlay

import android.content.Context
import android.graphics.Rect
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
            val bounds = boundsOf(cell, width, height)
            view.measure(
                MeasureSpec.makeMeasureSpec(bounds.width(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(bounds.height(), MeasureSpec.EXACTLY),
            )
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        cells.forEach { (view, cell) ->
            val bounds = boundsOf(cell, right - left, bottom - top)
            view.layout(bounds.left, bounds.top, bounds.right, bounds.bottom)
        }
    }

    private fun boundsOf(cell: GridCell, width: Int, height: Int): Rect {
        val cellWidth = (width - gapPx * (PageGrid.COLUMNS - 1)) / PageGrid.COLUMNS
        val cellHeight = (height - gapPx * (PageGrid.ROWS - 1)) / PageGrid.ROWS
        val left = cell.column * (cellWidth + gapPx)
        val top = cell.row * (cellHeight + gapPx)
        return Rect(
            left,
            top,
            left + cell.columnSpan * cellWidth + (cell.columnSpan - 1) * gapPx,
            top + cell.rowSpan * cellHeight + (cell.rowSpan - 1) * gapPx,
        )
    }
}
