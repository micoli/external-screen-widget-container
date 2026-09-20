package org.micoli.coverwidgetcontainer.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.micoli.coverwidgetcontainer.R
import org.micoli.coverwidgetcontainer.data.Container
import org.micoli.coverwidgetcontainer.data.GridCell
import org.micoli.coverwidgetcontainer.host.HostedWidgetManager
import org.micoli.coverwidgetcontainer.host.PageNavigation

class OverlayWindow(
    private val windowContext: Context,
    private val manager: HostedWidgetManager,
    val displayId: Int,
) {
    private val windowManager = windowContext.getSystemService(WindowManager::class.java)
    private val density = windowContext.resources.displayMetrics.density
    private val root = LinearLayout(windowContext)
    private val slots = mutableListOf<Pair<FrameLayout, Int>>()
    private var container: Container? = null
    private var pageIndex = 0
    private var shownBounds: Rect? = null

    val isShown: Boolean get() = shownBounds != null

    init {
        root.orientation = LinearLayout.VERTICAL
        root.outlineProvider = ViewOutlineProvider.BACKGROUND
        root.clipToOutline = true
        root.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            cornerRadius = CORNER_DP * density
        }
    }

    fun bind(newContainer: Container) {
        container = newContainer
        pageIndex = pageIndex.coerceIn(0, newContainer.pages.lastIndex)
        if (isShown) rebuild()
    }

    fun show(bounds: Rect) {
        if (bounds == shownBounds) return
        val params = layoutParams(bounds)
        if (shownBounds == null) {
            rebuild()
            windowManager.addView(root, params)
        } else {
            windowManager.updateViewLayout(root, params)
        }
        shownBounds = Rect(bounds)
    }

    fun hide() {
        if (shownBounds == null) return
        clearSlots()
        windowManager.removeView(root)
        shownBounds = null
    }

    private fun matchWeight() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)

    private fun layoutParams(bounds: Rect) = WindowManager.LayoutParams(
        bounds.width(),
        bounds.height(),
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = bounds.left
        y = bounds.top
    }

    private fun rebuild() {
        clearSlots()
        val current = container ?: return
        val page = current.pages[pageIndex]
        if (page.widgetIds.isEmpty()) {
            root.addView(emptyLabel(), matchWeight())
        } else {
            addGrid(page.widgetIds, page.placement())
        }
        if (current.pages.size > 1) root.addView(navigationBar(current.pages.size))
    }

    private fun addGrid(widgetIds: List<Int>, placement: List<GridCell?>) {
        val grid = GridPageLayout(windowContext, (GAP_DP * density).toInt())
        widgetIds.zip(placement).forEach { (appWidgetId, cell) ->
            if (cell != null) addSlot(grid, appWidgetId, cell)
        }
        root.addView(grid, matchWeight())
    }

    private fun addSlot(grid: GridPageLayout, appWidgetId: Int, cell: GridCell) {
        val slot = FrameLayout(windowContext)
        slot.addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
            if (right <= left || bottom <= top) return@addOnLayoutChangeListener
            manager.view(appWidgetId)?.applySize((right - left) / density, (bottom - top) / density)
        }
        grid.addCell(slot, cell)
        manager.attach(slot, appWidgetId)
        slots += slot to appWidgetId
    }

    private fun clearSlots() {
        slots.forEach { (slot, appWidgetId) -> manager.detach(slot, appWidgetId) }
        slots.clear()
        root.removeAllViews()
    }

    private fun emptyLabel() = TextView(windowContext).apply {
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        setText(R.string.widget_empty)
    }

    private fun navigationBar(pageCount: Int): View {
        val bar = LinearLayout(windowContext)
        bar.orientation = LinearLayout.HORIZONTAL
        bar.gravity = Gravity.CENTER
        bar.setBackgroundColor(NAV_BACKGROUND)
        bar.addView(navButton("‹", -1, pageCount))
        bar.addView(TextView(windowContext).apply {
            text = "${pageIndex + 1}/$pageCount"
            setTextColor(Color.WHITE)
        })
        bar.addView(navButton("›", 1, pageCount))
        return bar
    }

    private fun navButton(label: String, delta: Int, pageCount: Int) = TextView(windowContext).apply {
        text = label
        textSize = NAV_TEXT_SP
        setTextColor(Color.WHITE)
        val padding = (NAV_PADDING_DP * density).toInt()
        setPadding(padding, padding / 2, padding, padding / 2)
        setOnClickListener {
            pageIndex = PageNavigation.wrap(pageIndex, delta, pageCount)
            rebuild()
        }
    }

    private companion object {
        const val CORNER_DP = 20f
        const val GAP_DP = 6f
        const val NAV_PADDING_DP = 20f
        const val NAV_TEXT_SP = 22f
        val NAV_BACKGROUND = Color.argb(0x66, 0, 0, 0)
    }
}
