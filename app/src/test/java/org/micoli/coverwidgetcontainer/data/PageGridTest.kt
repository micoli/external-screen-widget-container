package org.micoli.coverwidgetcontainer.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PageGridTest {
    private fun place(vararg sizes: WidgetSize) = PageGrid.place(sizes.toList())

    @Test
    fun `a full page widget takes the whole grid`() {
        assertEquals(listOf(GridCell(0, 0, 2, 2)), place(WidgetSize.TWO_BY_TWO))
    }

    @Test
    fun `two strips stack vertically`() {
        assertEquals(
            listOf(GridCell(0, 0, 1, 2), GridCell(1, 0, 1, 2)),
            place(WidgetSize.ONE_BY_TWO, WidgetSize.ONE_BY_TWO),
        )
    }

    @Test
    fun `small widgets fill rows left to right`() {
        assertEquals(
            listOf(GridCell(0, 0, 1, 1), GridCell(0, 1, 1, 1), GridCell(1, 0, 1, 2)),
            place(WidgetSize.ONE_BY_ONE, WidgetSize.ONE_BY_ONE, WidgetSize.ONE_BY_TWO),
        )
    }

    @Test
    fun `a strip skips a half filled row and goes below`() {
        assertEquals(
            listOf(GridCell(0, 0, 1, 1), GridCell(1, 0, 1, 2)),
            place(WidgetSize.ONE_BY_ONE, WidgetSize.ONE_BY_TWO),
        )
    }

    @Test
    fun `a widget with no room is not placed`() {
        val placement = place(WidgetSize.TWO_BY_TWO, WidgetSize.ONE_BY_ONE)
        assertEquals(GridCell(0, 0, 2, 2), placement[0])
        assertNull(placement[1])
    }
}
