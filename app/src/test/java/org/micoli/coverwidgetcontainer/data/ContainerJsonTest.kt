package org.micoli.coverwidgetcontainer.data

import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContainerJsonTest {
    @Test
    fun `round trips a container with sizes`() {
        val container = Container(
            index = 3,
            name = "Kitchen",
            pages = listOf(
                Page(listOf(10, 11), mapOf(11 to WidgetSize.TWO_BY_TWO)),
                Page(listOf(12)),
            ),
        )
        val json = appJson.encodeToString(container)
        assertEquals(container, appJson.decodeFromString<Container>(json))
    }

    @Test
    fun `stores sizes by widget id and enum name`() {
        val json = appJson.encodeToString(Page(listOf(7), mapOf(7 to WidgetSize.ONE_BY_ONE)))
        assertTrue(json, json.contains("\"7\":\"ONE_BY_ONE\""))
    }

    @Test
    fun `reads data saved before sizes existed`() {
        val legacy = """{"index":1,"name":"Container 1","pages":[{"widgetIds":[90],"heights":{"90":2.0}}]}"""
        val container = appJson.decodeFromString<Container>(legacy)
        assertEquals(listOf(90), container.pages[0].widgetIds)
        assertEquals(emptyMap<Int, WidgetSize>(), container.pages[0].sizes)
        assertEquals(WidgetSize.DEFAULT, container.pages[0].sizeOf(90))
    }

    @Test
    fun `a container saved without pages gets one empty page`() {
        val container = appJson.decodeFromString<Container>("""{"index":2,"name":"Container 2"}""")
        assertEquals(listOf(Page()), container.pages)
    }

    @Test
    fun `round trips the widget library entry`() {
        val widget = HostedWidget(94, "com.sec.android.daemonapp/Weather", "Météo")
        assertEquals(widget, appJson.decodeFromString<HostedWidget>(appJson.encodeToString(widget)))
    }
}
