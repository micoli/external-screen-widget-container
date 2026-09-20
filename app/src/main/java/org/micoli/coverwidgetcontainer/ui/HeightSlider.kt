package org.micoli.coverwidgetcontainer.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.util.Locale
import org.micoli.coverwidgetcontainer.R
import org.micoli.coverwidgetcontainer.data.Page

private const val STEP = 0.25f
private const val STEP_COUNT = ((Page.MAX_HEIGHT - Page.MIN_HEIGHT) / STEP).toInt() - 1

@Composable
fun HeightSlider(height: Float, onCommit: (Float) -> Unit) {
    var dragged by remember(height) { mutableFloatStateOf(height) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.editor_widget_height, String.format(Locale.ROOT, "%.2f", dragged)),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(96.dp),
        )
        Slider(
            value = dragged,
            onValueChange = { dragged = it },
            onValueChangeFinished = { onCommit(dragged) },
            valueRange = Page.MIN_HEIGHT..Page.MAX_HEIGHT,
            steps = STEP_COUNT,
        )
    }
}
