package org.micoli.coverwidgetcontainer.cover

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.micoli.coverwidgetcontainer.R
import org.micoli.coverwidgetcontainer.host.HostedWidgetManager

class SnapshotService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var manager: HostedWidgetManager

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        manager = HostedWidgetManager.get(this)
        manager.acquireListening()
        scope.launch { ContainerRenderer.refreshAllPlaced(this@SnapshotService) }
        scope.launch { observeWidgetChanges() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        manager.releaseListening()
        super.onDestroy()
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeWidgetChanges() {
        manager.changes.debounce(DEBOUNCE_MS).collect {
            ContainerRenderer.refreshAllPlaced(this)
        }
    }

    private fun buildNotification(): Notification {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.service_channel_name), NotificationManager.IMPORTANCE_MIN),
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.service_notification_title))
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "SnapshotService"
        private const val CHANNEL_ID = "snapshot_service"
        private const val NOTIFICATION_ID = 1
        private const val DEBOUNCE_MS = 500L

        fun start(context: Context) {
            try {
                ContextCompat.startForegroundService(context, Intent(context, SnapshotService::class.java))
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Foreground start not allowed from background, open the app once to enable live updates", e)
            }
        }
    }
}
