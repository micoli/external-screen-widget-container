package org.micoli.coverwidgetcontainer.overlay

import java.util.Locale
import org.micoli.coverwidgetcontainer.data.ContainerIndex

// The cover widget carries this description so the accessibility service can find it in the launcher.
object WidgetMarker {
    const val PREFIX = "cw-container-"

    fun contentDescription(containerIndex: Int): String =
        String.format(Locale.ROOT, "%s%02d", PREFIX, containerIndex)

    private val hostLabelPattern = Regex("""^Container (\d{1,2})$""")

    // The launcher labels the host view of a widget with the provider label ("Container 7").
    fun parseHostLabel(description: CharSequence?): Int? {
        val digits = hostLabelPattern.find(description?.toString() ?: return null)?.groupValues?.get(1) ?: return null
        return digits.toInt().takeIf { it in 1..ContainerIndex.COUNT }
    }

    fun parse(description: CharSequence?): Int? {
        val text = description?.toString() ?: return null
        if (!text.startsWith(PREFIX)) return null
        return text.removePrefix(PREFIX).toIntOrNull()?.takeIf { it in 1..ContainerIndex.COUNT }
    }
}
