package org.micoli.coverwidgetcontainer.data

import kotlinx.serialization.Serializable

@Serializable
data class Page(
    val widgetIds: List<Int> = emptyList(),
    val sizes: Map<Int, WidgetSize> = emptyMap(),
) {
    fun sizeOf(appWidgetId: Int): WidgetSize = sizes[appWidgetId] ?: WidgetSize.DEFAULT

    fun placement(): List<GridCell?> = PageGrid.place(widgetIds.map(::sizeOf))
}

@Serializable
data class Container(
    val index: Int,
    val name: String,
    val pages: List<Page> = listOf(Page()),
) {
    val widgetCount: Int get() = pages.sumOf { it.widgetIds.size }

    fun withName(newName: String): Container = copy(name = newName)

    fun withPageAdded(): Container = copy(pages = pages + Page())

    fun withPageRemoved(pageIndex: Int): Container {
        if (pages.size <= 1 || pageIndex !in pages.indices) return this
        return copy(pages = pages.filterIndexed { i, _ -> i != pageIndex })
    }

    fun withWidgetAdded(pageIndex: Int, appWidgetId: Int): Container =
        updatePage(pageIndex) { it.copy(widgetIds = it.widgetIds + appWidgetId) }

    fun withWidgetRemoved(pageIndex: Int, appWidgetId: Int): Container =
        updatePage(pageIndex) { it.copy(widgetIds = it.widgetIds - appWidgetId, sizes = it.sizes - appWidgetId) }

    fun withoutWidget(appWidgetId: Int): Container =
        copy(pages = pages.map { it.copy(widgetIds = it.widgetIds - appWidgetId, sizes = it.sizes - appWidgetId) })

    fun withWidgetSize(pageIndex: Int, appWidgetId: Int, size: WidgetSize): Container {
        val page = pages.getOrNull(pageIndex) ?: return this
        if (appWidgetId !in page.widgetIds) return this
        return updatePage(pageIndex) { it.copy(sizes = it.sizes + (appWidgetId to size)) }
    }

    fun withWidgetMoved(pageIndex: Int, appWidgetId: Int, offset: Int): Container {
        val ids = pages.getOrNull(pageIndex)?.widgetIds ?: return this
        val from = ids.indexOf(appWidgetId)
        if (from < 0) return this
        val to = (from + offset).coerceIn(0, ids.lastIndex)
        if (to == from) return this
        val reordered = ids.toMutableList().apply {
            removeAt(from)
            add(to, appWidgetId)
        }
        return updatePage(pageIndex) { it.copy(widgetIds = reordered) }
    }

    private fun updatePage(pageIndex: Int, transform: (Page) -> Page): Container {
        if (pageIndex !in pages.indices) return this
        return copy(pages = pages.mapIndexed { i, page -> if (i == pageIndex) transform(page) else page })
    }

    companion object {
        fun default(index: Int): Container = Container(index = index, name = "Container $index")

        fun ensureAll(stored: List<Container>): List<Container> =
            (1..ContainerIndex.COUNT).map { i -> stored.firstOrNull { it.index == i } ?: default(i) }
    }
}
