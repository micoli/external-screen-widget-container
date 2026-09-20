package org.micoli.coverwidgetcontainer.ui

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_PREVIEW_PX = 512
private const val ICON_FRACTION = 0.5f

private class Preview(val image: ImageBitmap, val isIcon: Boolean)

@Composable
fun WidgetPreviewImage(info: AppWidgetProviderInfo, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val preview by produceState<Preview?>(initialValue = null, info) {
        value = withContext(Dispatchers.IO) { loadPreview(context, info) }
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        val loaded = preview ?: return@Box
        val imageModifier = if (loaded.isIcon) Modifier.fillMaxSize(ICON_FRACTION) else Modifier.matchParentSize()
        Image(bitmap = loaded.image, contentDescription = null, contentScale = ContentScale.Fit, modifier = imageModifier)
    }
}

// Widgets that ship no preview image fall back to their icon.
private fun loadPreview(context: Context, info: AppWidgetProviderInfo): Preview? {
    val densityDpi = context.resources.displayMetrics.densityDpi
    val preview = runCatching { info.loadPreviewImage(context, densityDpi) }.getOrNull()
    if (preview != null) return Preview(preview.toBitmap().asImageBitmap(), isIcon = false)
    val icon = runCatching { info.loadIcon(context, densityDpi) }.getOrNull() ?: return null
    return Preview(icon.toBitmap().asImageBitmap(), isIcon = true)
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
