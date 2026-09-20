package org.micoli.coverwidgetcontainer.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.micoli.coverwidgetcontainer.overlay.SettleTracker.State

class SettleTrackerTest {
    private val tracker = SettleTracker(settleMs = 150, tolerancePx = 3)
    private val page = Bounds(100, 50, 900, 820)

    @Test
    fun `first sighting starts the settling period`() {
        assertEquals(State.Settling(150), tracker.observe(1, page, nowMs = 1_000))
    }

    @Test
    fun `reports the time left while bounds stay put`() {
        tracker.observe(1, page, 1_000)
        assertEquals(State.Settling(50), tracker.observe(1, page, 1_100))
    }

    @Test
    fun `settles once the bounds held for the whole period`() {
        tracker.observe(1, page, 1_000)
        assertEquals(State.Settled, tracker.observe(1, page, 1_150))
        assertEquals(State.Settled, tracker.observe(1, page, 5_000))
    }

    @Test
    fun `ignores jitter within the tolerance`() {
        tracker.observe(1, page, 1_000)
        val jitter = Bounds(page.left + 3, page.top - 3, page.right + 2, page.bottom)
        assertEquals(State.Settled, tracker.observe(1, jitter, 1_200))
    }

    @Test
    fun `restarts the period when the bounds move`() {
        tracker.observe(1, page, 1_000)
        val sliding = Bounds(page.left - 40, page.top, page.right - 40, page.bottom)
        assertEquals(State.Settling(150), tracker.observe(1, sliding, 1_120))
        assertEquals(State.Settling(100), tracker.observe(1, sliding, 1_170))
        assertEquals(State.Settled, tracker.observe(1, sliding, 1_270))
    }

    @Test
    fun `a move just past the tolerance counts as movement`() {
        tracker.observe(1, page, 1_000)
        val moved = page.copy(left = page.left + 4)
        assertEquals(State.Settling(150), tracker.observe(1, moved, 1_200))
    }

    @Test
    fun `tracks each widget on its own`() {
        tracker.observe(1, page, 1_000)
        assertEquals(State.Settling(150), tracker.observe(2, page, 1_200))
        assertEquals(State.Settled, tracker.observe(1, page, 1_200))
    }

    @Test
    fun `forgetting a widget starts over`() {
        tracker.observe(1, page, 1_000)
        tracker.forget(1)
        assertEquals(State.Settling(150), tracker.observe(1, page, 1_500))
    }

    @Test
    fun `bounds closeness is checked on every edge`() {
        assertTrue(page.isCloseTo(page.copy(bottom = page.bottom + 3), 3))
        assertFalse(page.isCloseTo(page.copy(bottom = page.bottom + 4), 3))
        assertFalse(page.isCloseTo(page.copy(top = page.top - 4), 3))
        assertFalse(page.isCloseTo(page.copy(right = page.right - 4), 3))
    }
}
