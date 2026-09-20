package org.micoli.coverwidgetcontainer.host

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PageLayoutTest {
    @Test
    fun `splits height between slots with gaps`() {
        val slots = PageLayout.slots(count = 2, width = 100, height = 106, gap = 6)
        assertEquals(listOf(SlotBounds(0, 0, 100, 50), SlotBounds(0, 56, 100, 50)), slots)
    }

    @Test
    fun `single slot fills the page`() {
        assertEquals(listOf(SlotBounds(0, 0, 80, 60)), PageLayout.slots(1, 80, 60, 6))
    }

    @Test
    fun `finds slot under a point and ignores the gap`() {
        val slots = PageLayout.slots(2, 100, 106, 6)
        assertEquals(0, PageLayout.slotIndexAt(slots, 10f, 49f))
        assertEquals(1, PageLayout.slotIndexAt(slots, 10f, 60f))
        assertNull(PageLayout.slotIndexAt(slots, 10f, 52f))
    }
}
