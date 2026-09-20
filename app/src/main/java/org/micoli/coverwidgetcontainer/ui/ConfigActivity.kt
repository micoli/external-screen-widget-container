package org.micoli.coverwidgetcontainer.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import org.micoli.coverwidgetcontainer.R
import org.micoli.coverwidgetcontainer.host.AddWidgetFlow

class ConfigActivity : ComponentActivity() {
    private val viewModel by viewModels<ConfigViewModel>()
    private var addTarget: AddTarget? = null

    private val addWidgetFlow by lazy {
        AddWidgetFlow(
            activity = this,
            manager = viewModel.manager,
            onAdded = { widget ->
                viewModel.addToLibrary(widget, addTarget)
                addTarget = null
            },
            onFailed = {
                addTarget = null
                Toast.makeText(this, R.string.add_widget_failed, Toast.LENGTH_SHORT).show()
            },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.manager.bindToLifecycle(lifecycle)
        setContent {
            AppTheme {
                ConfigApp(
                    viewModel = viewModel,
                    onPickProvider = { info, target ->
                        addTarget = target
                        addWidgetFlow.start(info)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPlaced()
    }

    @Deprecated("Widget bind/configure flows only report through onActivityResult")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (addWidgetFlow.handleActivityResult(requestCode, resultCode)) return
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
    }
}
