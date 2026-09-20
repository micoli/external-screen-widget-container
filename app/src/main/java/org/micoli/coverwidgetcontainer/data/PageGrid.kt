package org.micoli.coverwidgetcontainer.data

data class GridCell(val row: Int, val column: Int, val rowSpan: Int, val columnSpan: Int)

data class CellBounds(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
}

object PageGrid {
    const val ROWS = 2
    const val COLUMNS = 2

    // Places widgets in order, each in the first free spot where it fits. A widget with no room gets null.
    fun place(sizes: List<WidgetSize>): List<GridCell?> {
        val occupied = Array(ROWS) { BooleanArray(COLUMNS) }
        return sizes.map { size -> findSpot(occupied, size)?.also { markOccupied(occupied, it) } }
    }

    // Pixel rectangle of a cell in a page of the given size, with gapPx between cells.
    fun cellBounds(cell: GridCell, width: Int, height: Int, gapPx: Int): CellBounds {
        val cellWidth = (width - gapPx * (COLUMNS - 1)) / COLUMNS
        val cellHeight = (height - gapPx * (ROWS - 1)) / ROWS
        val left = cell.column * (cellWidth + gapPx)
        val top = cell.row * (cellHeight + gapPx)
        return CellBounds(
            left,
            top,
            left + cell.columnSpan * cellWidth + (cell.columnSpan - 1) * gapPx,
            top + cell.rowSpan * cellHeight + (cell.rowSpan - 1) * gapPx,
        )
    }

    private fun findSpot(occupied: Array<BooleanArray>, size: WidgetSize): GridCell? {
        for (row in 0..ROWS - size.rows) {
            for (column in 0..COLUMNS - size.columns) {
                val cell = GridCell(row, column, size.rows, size.columns)
                if (isFree(occupied, cell)) return cell
            }
        }
        return null
    }

    private fun isFree(occupied: Array<BooleanArray>, cell: GridCell): Boolean =
        (cell.row until cell.row + cell.rowSpan).all { r ->
            (cell.column until cell.column + cell.columnSpan).none { c -> occupied[r][c] }
        }

    private fun markOccupied(occupied: Array<BooleanArray>, cell: GridCell) {
        for (r in cell.row until cell.row + cell.rowSpan) {
            for (c in cell.column until cell.column + cell.columnSpan) occupied[r][c] = true
        }
    }
}
