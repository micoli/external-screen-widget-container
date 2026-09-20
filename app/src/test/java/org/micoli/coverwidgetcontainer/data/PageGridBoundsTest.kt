package org.micoli.coverwidgetcontainer.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PageGridBoundsTest {
    private fun bounds(cell: GridCell, width: Int = 100, height: Int = 100, gap: Int = 4) =
        PageGrid.cellBounds(cell, width, height, gap)

    @Test
    fun `a single cell covers a quarter of the page`() {
        assertEquals(CellBounds(0, 0, 48, 48), bounds(GridCell(0, 0, 1, 1)))
        assertEquals(CellBounds(52, 0, 100, 48), bounds(GridCell(0, 1, 1, 1)))
        assertEquals(CellBounds(0, 52, 48, 100), bounds(GridCell(1, 0, 1, 1)))
    }

    @Test
    fun `a strip spans both columns including the gap`() {
        assertEquals(CellBounds(0, 52, 100, 100), bounds(GridCell(1, 0, 1, 2)))
    }

    @Test
    fun `a full page cell covers the whole page`() {
        val full = bounds(GridCell(0, 0, 2, 2))
        assertEquals(CellBounds(0, 0, 100, 100), full)
        assertEquals(100, full.width)
        assertEquals(100, full.height)
    }

    @Test
    fun `cells never leave the page when the size is odd`() {
        val page = bounds(GridCell(0, 0, 2, 2), width = 101, height = 103, gap = 5)
        assertTrue(page.right <= 101)
        assertTrue(page.bottom <= 103)
    }

    @Test
    fun `neighbouring cells are separated by exactly the gap`() {
        val left = bounds(GridCell(0, 0, 1, 1))
        val right = bounds(GridCell(0, 1, 1, 1))
        assertEquals(4, right.left - left.right)
    }

    @Test
    fun `without a gap the cells tile the page`() {
        val left = bounds(GridCell(0, 0, 1, 1), gap = 0)
        val right = bounds(GridCell(0, 1, 1, 1), gap = 0)
        assertEquals(left.right, right.left)
        assertEquals(50, left.width)
    }
}
