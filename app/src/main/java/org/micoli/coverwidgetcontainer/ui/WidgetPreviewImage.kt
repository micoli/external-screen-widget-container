package org.micoli.coverwidgetcontainer.ui

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_PREVIEW_PX = 512

@Composable
fun WidgetPreviewImage(info: AppWidgetProviderInfo, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val preview by produceState<ImageBitmap?>(initialValue = null, info) {
        value = withContext(Dispatchers.IO) { loadPreview(context, info) }
    }
    Box(modifier) {
        val image = preview ?: return@Box
        Image(bitmap = image, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.matchParentSize())
    }
}

// Widgets that ship no preview image fall back to their icon.
private fun loadPreview(context: Context, info: AppWidgetProviderInfo): ImageBitmap? {
    val densityDpi = context.resources.displayMetrics.densityDpi
    val drawable = runCatching { info.loadPreviewImage(context, densityDpi) }.getOrNull()
        ?: runCatching { info.loadIcon(context, densityDpi) }.getOrNull()
        ?: return null
    return drawable.toBitmap().asImageBitmap()
}

private fun Drawable.toBitmap(): Bitmap {
    val scale = minOf(1f, MAX_PREVIEW_PX.toFloat() / maxOf(intrinsicWidth, intrinsicHeight, 1))
    val width = (intrinsicWidth * scale).toInt().coerceAtLeast(1)
    val height = (intrinsicHeight * scale).toInt().coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    setBounds(0, 0, width, height)
    draw(Canvas(bitmap))
    return bitmap
}
