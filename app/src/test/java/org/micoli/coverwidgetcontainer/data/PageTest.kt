package org.micoli.coverwidgetcontainer.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PageTest {
    @Test
    fun `a widget with no chosen size gets the default strip`() {
        assertEquals(WidgetSize.ONE_BY_TWO, Page(widgetIds = listOf(1)).sizeOf(1))
    }

    @Test
    fun `placement follows the chosen sizes in order`() {
        val page = Page(
            widgetIds = listOf(1, 2, 3),
            sizes = mapOf(1 to WidgetSize.ONE_BY_ONE, 2 to WidgetSize.ONE_BY_ONE),
        )
        assertEquals(
            listOf(GridCell(0, 0, 1, 1), GridCell(0, 1, 1, 1), GridCell(1, 0, 1, 2)),
            page.placement(),
        )
    }

    @Test
    fun `a third default strip has no room`() {
        val placement = Page(widgetIds = listOf(1, 2, 3)).placement()
        assertEquals(GridCell(0, 0, 1, 2), placement[0])
        assertEquals(GridCell(1, 0, 1, 2), placement[1])
        assertNull(placement[2])
    }

    @Test
    fun `an empty page has no placement`() {
        assertEquals(emptyList<GridCell?>(), Page().placement())
    }
}
