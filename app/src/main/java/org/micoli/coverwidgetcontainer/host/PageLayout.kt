package org.micoli.coverwidgetcontainer.host

data class SlotBounds(val left: Int, val top: Int, val width: Int, val height: Int) {
    fun contains(x: Float, y: Float): Boolean =
        x >= left && x < left + width && y >= top && y < top + height
}

object PageLayout {
    const val GAP_DP = 6f

    fun slots(count: Int, width: Int, height: Int, gap: Int): List<SlotBounds> {
        if (count <= 0) return emptyList()
        val slotHeight = (height - gap * (count - 1)) / count
        return List(count) { position -> SlotBounds(0, position * (slotHeight + gap), width, slotHeight) }
    }

    fun slotIndexAt(slots: List<SlotBounds>, x: Float, y: Float): Int? =
        slots.indexOfFirst { it.contains(x, y) }.takeIf { it >= 0 }
}
