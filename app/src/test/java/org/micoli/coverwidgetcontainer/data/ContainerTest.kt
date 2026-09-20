package org.micoli.coverwidgetcontainer.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ContainerTest {
    private val container = Container.default(1)

    @Test
    fun `default container has one empty page`() {
        assertEquals(listOf(Page()), container.pages)
        assertEquals("Container 1", container.name)
    }

    @Test
    fun `adds and removes a widget on a page`() {
        val added = container.withWidgetAdded(0, 42)
        assertEquals(listOf(42), added.pages[0].widgetIds)
        assertEquals(1, added.widgetCount)
        assertEquals(emptyList<Int>(), added.withWidgetRemoved(0, 42).pages[0].widgetIds)
    }

    @Test
    fun `ignores widget added on unknown page`() {
        assertSame(container, container.withWidgetAdded(3, 42))
    }

    @Test
    fun `never removes the last page`() {
        assertSame(container, container.withPageRemoved(0))
        assertEquals(1, container.withPageAdded().withPageRemoved(1).pages.size)
    }

    @Test
    fun `moves widget within bounds`() {
        val filled = container.withWidgetAdded(0, 1).withWidgetAdded(0, 2).withWidgetAdded(0, 3)
        assertEquals(listOf(2, 1, 3), filled.withWidgetMoved(0, 1, 1).pages[0].widgetIds)
        assertEquals(listOf(3, 1, 2), filled.withWidgetMoved(0, 3, -10).pages[0].widgetIds)
        assertSame(filled, filled.withWidgetMoved(0, 1, -1))
    }

    @Test
    fun `removes a widget from every page`() {
        val filled = container.withPageAdded().withWidgetAdded(0, 7).withWidgetAdded(1, 7)
        assertEquals(0, filled.withoutWidget(7).widgetCount)
    }

    @Test
    fun `ensureAll fills missing containers and keeps stored ones`() {
        val stored = Container.default(5).withName("Kitchen")
        val all = Container.ensureAll(listOf(stored))
        assertEquals(ContainerIndex.COUNT, all.size)
        assertEquals("Kitchen", all[4].name)
        assertEquals("Container 6", all[5].name)
    }

    @Test
    fun `sets a widget height clamped to the allowed range`() {
        val filled = container.withWidgetAdded(0, 7)
        assertEquals(1f, filled.pages[0].heightOf(7), 0f)
        assertEquals(2f, filled.withWidgetHeight(0, 7, 2f).pages[0].heightOf(7), 0f)
        assertEquals(Page.MAX_HEIGHT, filled.withWidgetHeight(0, 7, 99f).pages[0].heightOf(7), 0f)
        assertEquals(Page.MIN_HEIGHT, filled.withWidgetHeight(0, 7, 0f).pages[0].heightOf(7), 0f)
    }

    @Test
    fun `ignores height for a widget not on the page and drops it on removal`() {
        val filled = container.withWidgetAdded(0, 7)
        assertSame(filled, filled.withWidgetHeight(0, 8, 2f))
        assertEquals(1f, filled.withWidgetHeight(0, 7, 2f).withWidgetRemoved(0, 7).pages[0].heightOf(7), 0f)
        assertEquals(emptyMap<Int, Float>(), filled.withWidgetHeight(0, 7, 2f).withoutWidget(7).pages[0].heights)
    }
}
