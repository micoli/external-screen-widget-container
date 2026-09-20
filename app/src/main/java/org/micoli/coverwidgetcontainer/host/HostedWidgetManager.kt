package org.micoli.coverwidgetcontainer.host

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.view.ViewGroup
import java.util.concurrent.ConcurrentHashMap
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class HostedWidgetManager private constructor(private val context: Context) {
    private val appWidgetManager = AppWidgetManager.getInstance(context)
    private val host = object : AppWidgetHost(context, HOST_ID) {
        override fun onCreateView(
            context: Context,
            appWidgetId: Int,
            appWidget: AppWidgetProviderInfo?,
        ): AppWidgetHostView = SnapshotHostView(context)
    }

    // Main thread only: one host view per widget, so widget updates always reach the same instance.
    private val views = mutableMapOf<Int, SnapshotHostView>()
    private val attachedIds: MutableSet<Int> = ConcurrentHashMap.newKeySet()
    private var listenerCount = 0

    private val _changes = MutableSharedFlow<Int>(extraBufferCapacity = CHANGES_BUFFER)
    val changes: SharedFlow<Int> = _changes

    fun installedProviders(): List<AppWidgetProviderInfo> =
        appWidgetManager.installedProviders.filterNot { it.provider.packageName == context.packageName }

    fun allocateId(): Int = host.allocateAppWidgetId()

    fun delete(appWidgetId: Int) {
        views.remove(appWidgetId)
        host.deleteAppWidgetId(appWidgetId)
    }

    fun bindIfAllowed(appWidgetId: Int, provider: ComponentName): Boolean =
        appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, provider)

    fun bindIntent(appWidgetId: Int, provider: ComponentName): Intent =
        Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)

    fun startConfigure(activity: Activity, appWidgetId: Int, requestCode: Int) =
        host.startAppWidgetConfigureActivityForResult(activity, appWidgetId, 0, requestCode, null)

    fun view(appWidgetId: Int): SnapshotHostView? {
        views[appWidgetId]?.let { return it }
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return null
        val created = host.createView(ContextWrapper(context), appWidgetId, info) as SnapshotHostView
        created.onContentChanged = { _changes.tryEmit(appWidgetId) }
        views[appWidgetId] = created
        return created
    }

    // Attached views are laid out by a real window, so offscreen snapshots must leave them alone.
    fun isAttached(appWidgetId: Int): Boolean = appWidgetId in attachedIds

    fun attach(parent: ViewGroup, appWidgetId: Int) {
        val hostView = view(appWidgetId) ?: return
        (hostView.parent as? ViewGroup)?.removeView(hostView)
        parent.addView(hostView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        attachedIds += appWidgetId
    }

    fun detach(parent: ViewGroup, appWidgetId: Int) {
        val hostView = views[appWidgetId] ?: return
        if (hostView.parent === parent) parent.removeView(hostView)
        attachedIds -= appWidgetId
        _changes.tryEmit(appWidgetId)
    }

    fun acquireListening() {
        listenerCount += 1
        if (listenerCount == 1) host.startListening()
    }

    fun releaseListening() {
        listenerCount -= 1
        if (listenerCount == 0) host.stopListening()
    }

    fun bindToLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = acquireListening()
            override fun onStop(owner: LifecycleOwner) = releaseListening()
        })
    }

    companion object {
        private const val HOST_ID = 1024
        private const val CHANGES_BUFFER = 64

        @Volatile
        private var instance: HostedWidgetManager? = null

        fun get(context: Context): HostedWidgetManager =
            instance ?: synchronized(this) {
                instance ?: HostedWidgetManager(context.applicationContext).also { instance = it }
            }
    }
}
