package org.micoli.coverwidgetcontainer.overlay

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

// Read from the system setting rather than from the service itself, so a widget update never races the service start.
object OverlayState {

    fun isEnabled(context: Context): Boolean {
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?: return false
        val component = ComponentName(context, CoverOverlayService::class.java)
        return enabled.split(':').any { it == component.flattenToString() || it == component.flattenToShortString() }
    }
}
