package org.micoli.coverwidgetcontainer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.filter
import org.micoli.coverwidgetcontainer.R
import org.micoli.coverwidgetcontainer.host.HostedWidgetManager
import org.micoli.coverwidgetcontainer.host.WidgetSnapshotRenderer

@Composable
fun HostedWidgetView(
    appWidgetId: Int,
    manager: HostedWidgetManager,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    var size by remember { mutableStateOf(IntSize.Zero) }
    var preview by remember { mutableStateOf<ImageBitmap?>(null) }
    val version by produceState(initialValue = 0, appWidgetId) {
        manager.changes.filter { it == appWidgetId }.collect { value += 1 }
    }

    LaunchedEffect(appWidgetId, size, version) {
        if (size == IntSize.Zero) return@LaunchedEffect
        val view = manager.view(appWidgetId)
        preview = view?.let {
            WidgetSnapshotRenderer
                .renderWidget(it, size.width, size.height, density, WidgetSnapshotRenderer.PAGE_BACKGROUND)
                .asImageBitmap()
        }
    }

    Box(modifier.onSizeChanged { size = it }, contentAlignment = Alignment.Center) {
        val image = preview
        if (image == null) {
            Text(stringResource(R.string.widget_unavailable))
            return@Box
        }
        Image(bitmap = image, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
    }
}
