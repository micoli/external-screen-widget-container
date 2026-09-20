package org.micoli.coverwidgetcontainer.cover

import android.content.Context

class PageStateStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("cover_pages", Context.MODE_PRIVATE)

    fun current(appWidgetId: Int): Int = prefs.getInt(appWidgetId.toString(), 0)

    fun set(appWidgetId: Int, pageIndex: Int) = prefs.edit().putInt(appWidgetId.toString(), pageIndex).apply()
}
