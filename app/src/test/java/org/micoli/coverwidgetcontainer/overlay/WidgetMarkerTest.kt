package org.micoli.coverwidgetcontainer.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetMarkerTest {
    @Test
    fun `round trips a container index`() {
        assertEquals("cw-container-07", WidgetMarker.contentDescription(7))
        assertEquals(7, WidgetMarker.parse(WidgetMarker.contentDescription(7)))
    }

    @Test
    fun `rejects foreign or out of range descriptions`() {
        assertNull(WidgetMarker.parse("Clock"))
        assertNull(WidgetMarker.parse("cw-container-21"))
        assertNull(WidgetMarker.parse("cw-container-xx"))
        assertNull(WidgetMarker.parse(null))
    }

    @Test
    fun `parses the provider label used by the launcher host view`() {
        assertEquals(1, WidgetMarker.parseHostLabel("Container 1"))
        assertEquals(20, WidgetMarker.parseHostLabel("Container 20"))
        assertNull(WidgetMarker.parseHostLabel("Container 21"))
        assertNull(WidgetMarker.parseHostLabel("Prévisions météorologiques"))
    }
}
