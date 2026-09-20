package org.micoli.coverwidgetcontainer.overlay

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.util.Log
import android.os.Handler
import android.os.SystemClock
import android.os.Looper
import android.view.Display
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.micoli.coverwidgetcontainer.cover.ContainerWidgetUpdater
import org.micoli.coverwidgetcontainer.data.Container
import org.micoli.coverwidgetcontainer.data.ContainerRepository
import org.micoli.coverwidgetcontainer.host.HostedWidgetManager

class CoverOverlayService : AccessibilityService() {
    private class MarkedWidget(val displayId: Int, val bounds: Rect)
    private class TrackedBounds(val bounds: Rect, val since: Long)

    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val overlays = mutableMapOf<Int, OverlayWindow>()
    private val missedScans = mutableMapOf<Int, Int>()
    private val trackedBounds = mutableMapOf<Int, TrackedBounds>()
    private var containers = emptyList<Container>()
    private lateinit var manager: HostedWidgetManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        manager = HostedWidgetManager.get(this)
        manager.acquireListening()
        ContainerWidgetUpdater.refreshAll(this)
        scope.launch {
            ContainerRepository(applicationContext).containers.collect { latest ->
                containers = latest
                overlays.forEach { (index, overlay) -> overlay.bind(latest.first { it.index == index }) }
            }
        }
        scheduleScan(0)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val source = event.packageName?.toString()
        if (source == packageName) return
        scheduleScan(SCAN_DELAY_MS)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        overlays.values.forEach { it.hide() }
        overlays.clear()
        if (::manager.isInitialized) manager.releaseListening()
        ContainerWidgetUpdater.refreshAll(this)
        scope.cancel()
        super.onDestroy()
    }

    private fun scheduleScan(delayMs: Long) {
        handler.removeCallbacks(scanRunnable)
        handler.postDelayed(scanRunnable, delayMs)
    }

    private val scanRunnable = Runnable { scan() }

    private fun scan() {
        val found = findMarkedWidgets()
        found.forEach { (index, marked) ->
            missedScans.remove(index)
            showWhenSettled(index, marked)
        }
        val missing = overlays.keys.filter { it !in found }
        missing.forEach { index -> hideAfterMisses(index) }
        if (missing.any { overlays[it]?.isShown == true }) scheduleScan(FOLLOW_UP_MS)
    }

    private fun showOverlay(index: Int, marked: MarkedWidget) {
        val overlay = overlayFor(index, marked.displayId)
        if (overlay == null) {
            Log.w(TAG, "No overlay for container $index (containers loaded: ${containers.size})")
            return
        }
        try {
            overlay.show(marked.bounds)
        } catch (e: RuntimeException) {
            Log.e(TAG, "Overlay show failed for container $index on display ${marked.displayId}", e)
        }
    }

    // A widget can vanish from the tree for a frame while pages animate, so hiding needs consecutive misses.
    // While a page slides the launcher animates the widget, so its bounds keep changing: the overlay stays hidden
    // until the bounds have stopped moving, which keeps hosted widgets from being resized during the swipe.
    private fun showWhenSettled(index: Int, marked: MarkedWidget) {
        val now = SystemClock.uptimeMillis()
        val tracked = trackedBounds[index]
        if (tracked == null || !tracked.bounds.isCloseTo(marked.bounds)) {
            trackedBounds[index] = TrackedBounds(Rect(marked.bounds), now)
            overlays[index]?.hide()
            scheduleScan(SETTLE_MS)
            return
        }
        val remaining = SETTLE_MS - (now - tracked.since)
        if (remaining > 0) {
            scheduleScan(remaining)
            return
        }
        showOverlay(index, marked)
    }

    private fun Rect.isCloseTo(other: Rect): Boolean =
        kotlin.math.abs(left - other.left) <= BOUNDS_TOLERANCE_PX &&
            kotlin.math.abs(top - other.top) <= BOUNDS_TOLERANCE_PX &&
            kotlin.math.abs(right - other.right) <= BOUNDS_TOLERANCE_PX &&
            kotlin.math.abs(bottom - other.bottom) <= BOUNDS_TOLERANCE_PX

    private fun hideAfterMisses(index: Int) {
        val misses = (missedScans[index] ?: 0) + 1
        if (misses < MISSES_BEFORE_HIDE) {
            missedScans[index] = misses
            return
        }
        missedScans.remove(index)
        trackedBounds.remove(index)
        overlays[index]?.hide()
    }

    private fun overlayFor(index: Int, displayId: Int): OverlayWindow? {
        val existing = overlays[index]
        if (existing != null && existing.displayId == displayId) return existing
        existing?.hide()
        val container = containers.firstOrNull { it.index == index } ?: return null
        val display = getSystemService(DisplayManager::class.java).getDisplay(displayId) ?: return null
        val windowContext = createWindowContext(display, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, null)
        return OverlayWindow(windowContext, manager, displayId).also {
            it.bind(container)
            overlays[index] = it
        }
    }

    private fun findMarkedWidgets(): Map<Int, MarkedWidget> {
        val found = mutableMapOf<Int, MarkedWidget>()
        val windowsByDisplay = windowsOnAllDisplays
        val summary = StringBuilder()
        hasWindowWithoutRoot = false
        for (position in 0 until windowsByDisplay.size()) {
            val displayId = windowsByDisplay.keyAt(position)
            summary.append("d$displayId=${windowsByDisplay.valueAt(position).size} ")
            if (displayId == Display.DEFAULT_DISPLAY) continue
            for (window in windowsByDisplay.valueAt(position)) {
                val root = window.root
                if (root == null) {
                    summary.append("[no-root ${window.title}] ")
                    hasWindowWithoutRoot = true
                    continue
                }
                val pkg = root.packageName?.toString()
                summary.append("[$pkg ${window.title}] ")
                if (pkg == packageName) continue
                scanTree(root, displayId, found, summary)
            }
        }
        logIfChanged(summary.toString())
        retryWhileWindowsUnreadable()
        return found
    }

    // Widgets show up as AppWidgetHostView labelled with the provider label; our own views also carry a marker.
    private fun scanTree(root: AccessibilityNodeInfo, displayId: Int, found: MutableMap<Int, MarkedWidget>, summary: StringBuilder) {
        val queue = ArrayDeque<AccessibilityNodeInfo>().apply { add(root) }
        var visited = 0
        while (queue.isNotEmpty() && visited < MAX_SCANNED_NODES) {
            val node = queue.removeFirst()
            visited++
            val description = node.contentDescription
            val isHostView = node.className?.toString() == HOST_VIEW_CLASS
            if (isHostView) {
                val bounds = Rect().also(node::getBoundsInScreen)
                summary.append("HOST(${description} $bounds vis=${node.isVisibleToUser}) ")
            }
            val index = WidgetMarker.parse(description) ?: WidgetMarker.parseHostLabel(description).takeIf { isHostView }
            if (index != null && node.isVisibleToUser && index !in found) {
                val bounds = Rect().also(node::getBoundsInScreen)
                if (!bounds.isEmpty) {
                    found[index] = MarkedWidget(displayId, bounds)
                    summary.append("MARK#$index $bounds ")
                }
            }
            for (i in 0 until node.childCount) node.getChild(i)?.let(queue::add)
        }
        summary.append("(nodes=$visited) ")
    }

    // Only detections are logged: the full window summary is too chatty outside of a debugging session.
    private fun logIfChanged(summary: String) {
        if (!summary.contains("MARK#") || summary == lastSummary) return
        lastSummary = summary
        Log.d(TAG, "scan: $summary")
    }

    private var hasWindowWithoutRoot = false
    private var unreadableRetries = 0

    // A window can exist before its content is readable, so scan again shortly instead of waiting for an event.
    private fun retryWhileWindowsUnreadable() {
        if (!hasWindowWithoutRoot) {
            unreadableRetries = 0
            return
        }
        if (unreadableRetries >= MAX_UNREADABLE_RETRIES) return
        unreadableRetries += 1
        scheduleScan(UNREADABLE_RETRY_MS)
    }

    private var lastSummary = ""

    private companion object {
        const val TAG = "CoverOverlay"
        const val SCAN_DELAY_MS = 60L
        const val FOLLOW_UP_MS = 200L
        const val MISSES_BEFORE_HIDE = 2
        const val SETTLE_MS = 150L
        const val BOUNDS_TOLERANCE_PX = 3
        const val UNREADABLE_RETRY_MS = 250L
        const val MAX_UNREADABLE_RETRIES = 12
        const val MAX_SCANNED_NODES = 800
        const val HOST_VIEW_CLASS = "android.appwidget.AppWidgetHostView"
    }
}
