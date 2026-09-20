package org.micoli.coverwidgetcontainer.overlay

data class Bounds(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    fun isCloseTo(other: Bounds, tolerance: Int): Boolean =
        kotlin.math.abs(left - other.left) <= tolerance &&
            kotlin.math.abs(top - other.top) <= tolerance &&
            kotlin.math.abs(right - other.right) <= tolerance &&
            kotlin.math.abs(bottom - other.bottom) <= tolerance
}

// Tells when a widget has stopped moving: its bounds must stay put for settleMs before it counts as settled.
class SettleTracker(private val settleMs: Long, private val tolerancePx: Int) {

    sealed interface State {
        data class Settling(val remainingMs: Long) : State
        data object Settled : State
    }

    private class Tracked(val bounds: Bounds, val since: Long)

    private val tracked = mutableMapOf<Int, Tracked>()

    fun observe(key: Int, bounds: Bounds, nowMs: Long): State {
        val current = tracked[key]
        if (current == null || !current.bounds.isCloseTo(bounds, tolerancePx)) {
            tracked[key] = Tracked(bounds, nowMs)
            return State.Settling(settleMs)
        }
        val remaining = settleMs - (nowMs - current.since)
        return if (remaining > 0) State.Settling(remaining) else State.Settled
    }

    fun forget(key: Int) {
        tracked.remove(key)
    }
}
