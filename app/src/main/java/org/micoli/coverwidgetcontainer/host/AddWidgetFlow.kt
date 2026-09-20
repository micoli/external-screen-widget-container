package org.micoli.coverwidgetcontainer.host

import android.app.Activity
import android.appwidget.AppWidgetProviderInfo
import org.micoli.coverwidgetcontainer.data.HostedWidget

class AddWidgetFlow(
    private val activity: Activity,
    private val manager: HostedWidgetManager,
    private val onAdded: (HostedWidget) -> Unit,
    private val onFailed: () -> Unit,
) {
    private var pendingId: Int? = null
    private var pendingInfo: AppWidgetProviderInfo? = null

    fun start(info: AppWidgetProviderInfo) {
        val appWidgetId = manager.allocateId()
        pendingId = appWidgetId
        pendingInfo = info
        if (manager.bindIfAllowed(appWidgetId, info.provider)) {
            configureOrFinish()
            return
        }
        activity.startActivityForResult(manager.bindIntent(appWidgetId, info.provider), REQUEST_BIND)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int): Boolean {
        if (requestCode != REQUEST_BIND && requestCode != REQUEST_CONFIGURE) return false
        if (resultCode != Activity.RESULT_OK) {
            abort()
            return true
        }
        if (requestCode == REQUEST_BIND) configureOrFinish() else finish()
        return true
    }

    private fun configureOrFinish() {
        val appWidgetId = pendingId ?: return
        val needsConfiguration = pendingInfo?.configure != null
        if (!needsConfiguration) {
            finish()
            return
        }
        manager.startConfigure(activity, appWidgetId, REQUEST_CONFIGURE)
    }

    private fun finish() {
        val appWidgetId = pendingId ?: return
        val info = pendingInfo ?: return
        clear()
        onAdded(
            HostedWidget(
                appWidgetId = appWidgetId,
                provider = info.provider.flattenToString(),
                label = info.loadLabel(activity.packageManager),
            ),
        )
    }

    private fun abort() {
        pendingId?.let(manager::delete)
        clear()
        onFailed()
    }

    private fun clear() {
        pendingId = null
        pendingInfo = null
    }

    private companion object {
        const val REQUEST_BIND = 7001
        const val REQUEST_CONFIGURE = 7002
    }
}
