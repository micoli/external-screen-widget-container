package org.micoli.coverwidgetcontainer.host

import org.junit.Assert.assertEquals
import org.junit.Test

class PageNavigationTest {
    @Test
    fun `wraps forward and backward`() {
        assertEquals(1, PageNavigation.wrap(0, 1, 3))
        assertEquals(0, PageNavigation.wrap(2, 1, 3))
        assertEquals(2, PageNavigation.wrap(0, -1, 3))
    }

    @Test
    fun `returns first page when there are no pages`() {
        assertEquals(0, PageNavigation.wrap(5, 1, 0))
    }
}
