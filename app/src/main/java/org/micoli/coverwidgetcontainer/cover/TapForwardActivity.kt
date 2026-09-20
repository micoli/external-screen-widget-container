package org.micoli.coverwidgetcontainer.cover

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.os.Bundle

class TapForwardActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        val appWidgetId = intent.getIntExtra(TapGrid.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val column = intent.getIntExtra(TapGrid.EXTRA_COLUMN, -1)
        val row = intent.getIntExtra(TapGrid.EXTRA_ROW, -1)
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && column >= 0 && row >= 0) {
            TapDispatcher.dispatch(this, appWidgetId, column, row)
        }
        window.decorView.post { finish() }
    }
}
