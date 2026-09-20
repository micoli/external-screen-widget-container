package org.micoli.coverwidgetcontainer.cover

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.RemoteViews
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.micoli.coverwidgetcontainer.R
import org.micoli.coverwidgetcontainer.data.Container
import org.micoli.coverwidgetcontainer.data.ContainerIndex
import org.micoli.coverwidgetcontainer.data.ContainerRepository
import org.micoli.coverwidgetcontainer.host.HostedWidgetManager
import org.micoli.coverwidgetcontainer.host.PageLayout
import org.micoli.coverwidgetcontainer.host.PageNavigation
import org.micoli.coverwidgetcontainer.host.SlotBounds
import org.micoli.coverwidgetcontainer.host.WidgetSnapshotRenderer

class RenderGeometry(
    val widthPx: Int,
    val heightPx: Int,
    val widgetIds: List<Int>,
    val slots: List<SlotBounds>,
)

object ContainerRenderer {
    private class PushedState(val state: String, val bitmap: Bitmap)

    private const val DEFAULT_WIDTH_DP = 352
    private const val DEFAULT_HEIGHT_DP = 339

    private val lastPushed = ConcurrentHashMap<Int, PushedState>()
    private val geometries = ConcurrentHashMap<Int, RenderGeometry>()

    fun geometry(appWidgetId: Int): RenderGeometry? = geometries[appWidgetId]

    suspend fun update(context: Context, appWidgetId: Int, containerIndex: Int, force: Boolean = false) {
        val appContext = context.applicationContext
        val container = ContainerRepository(appContext).get(containerIndex)
        val pageStore = PageStateStore(appContext)
        val pageIndex = pageStore.current(appWidgetId).coerceIn(0, container.pages.lastIndex)
        pageStore.set(appWidgetId, pageIndex)

        val views = RemoteViews(appContext.packageName, R.layout.widget_container)
        val widgetIds = container.pages[pageIndex].widgetIds
        if (widgetIds.isEmpty()) {
            lastPushed.remove(appWidgetId)
            geometries.remove(appWidgetId)
            showPlaceholder(appContext, views, container)
        } else {
            val bitmap = renderBitmap(appContext, appWidgetId, widgetIds)
            val state = "${container.name}/$pageIndex/${container.pages.size}"
            if (!force && isAlreadyPushed(appWidgetId, state, bitmap)) return
            lastPushed[appWidgetId] = PushedState(state, bitmap)
            showBitmap(appContext, views, appWidgetId, bitmap)
        }
        showNavigation(appContext, views, container, appWidgetId, pageIndex)
        AppWidgetManager.getInstance(appContext).updateAppWidget(appWidgetId, views)
    }

    suspend fun turnPage(context: Context, appWidgetId: Int, containerIndex: Int, delta: Int) {
        val container = ContainerRepository(context.applicationContext).get(containerIndex)
        val store = PageStateStore(context)
        store.set(appWidgetId, PageNavigation.wrap(store.current(appWidgetId), delta, container.pages.size))
        update(context, appWidgetId, containerIndex)
    }

    suspend fun refreshAllPlaced(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        for (index in 1..ContainerIndex.COUNT) {
            val component = ContainerWidgetUpdater.componentFor(context, index)
            appWidgetManager.getAppWidgetIds(component).forEach { update(context, it, index) }
        }
    }

    private fun showPlaceholder(context: Context, views: RemoteViews, container: Container) {
        views.setViewVisibility(R.id.widget_image, View.GONE)
        views.setViewVisibility(R.id.widget_placeholder, View.VISIBLE)
        views.setTextViewText(R.id.widget_title, container.name)
        views.setTextViewText(R.id.widget_subtitle, context.getString(R.string.widget_empty))
        TapGrid.clear(views)
    }

    private fun isAlreadyPushed(appWidgetId: Int, state: String, bitmap: Bitmap): Boolean {
        val previous = lastPushed[appWidgetId] ?: return false
        return previous.state == state && previous.bitmap.sameAs(bitmap)
    }

    private suspend fun renderBitmap(context: Context, appWidgetId: Int, widgetIds: List<Int>): Bitmap {
        val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
        val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0).takeIf { it > 0 } ?: DEFAULT_WIDTH_DP
        val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0).takeIf { it > 0 } ?: DEFAULT_HEIGHT_DP
        val density = context.resources.displayMetrics.density
        val (widthPx, heightPx) = WidgetSnapshotRenderer.fitToBudget((widthDp * density).toInt(), (heightDp * density).toInt())
        val renderDensity = widthPx.toFloat() / widthDp
        val slots = PageLayout.slots(widgetIds.size, widthPx, heightPx, (PageLayout.GAP_DP * renderDensity).toInt())
        geometries[appWidgetId] = RenderGeometry(widthPx, heightPx, widgetIds, slots)

        return withContext(Dispatchers.Main) {
            WidgetSnapshotRenderer.renderPage(HostedWidgetManager.get(context), widgetIds, slots, widthPx, heightPx, renderDensity)
        }
    }

    private fun showBitmap(context: Context, views: RemoteViews, appWidgetId: Int, bitmap: Bitmap) {
        views.setImageViewBitmap(R.id.widget_image, bitmap)
        views.setViewVisibility(R.id.widget_image, View.VISIBLE)
        views.setViewVisibility(R.id.widget_placeholder, View.GONE)
        TapGrid.install(context, views, appWidgetId)
    }

    private fun showNavigation(context: Context, views: RemoteViews, container: Container, appWidgetId: Int, pageIndex: Int) {
        if (container.pages.size <= 1) {
            views.setViewVisibility(R.id.widget_nav, View.GONE)
            return
        }
        views.setViewVisibility(R.id.widget_nav, View.VISIBLE)
        views.setTextViewText(R.id.widget_page_label, "${pageIndex + 1}/${container.pages.size}")
        views.setOnClickPendingIntent(R.id.widget_prev, PageNavigation.pendingIntent(context, container.index, appWidgetId, -1))
        views.setOnClickPendingIntent(R.id.widget_next, PageNavigation.pendingIntent(context, container.index, appWidgetId, 1))
    }
}
