package org.micoli.coverwidgetcontainer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.micoli.coverwidgetcontainer.R
import org.micoli.coverwidgetcontainer.data.WidgetSize
import org.micoli.coverwidgetcontainer.host.HostedWidgetManager

@Composable
fun EditorWidgetRow(
    label: String,
    appWidgetId: Int,
    manager: HostedWidgetManager,
    size: WidgetSize,
    fits: Boolean,
    onResize: (WidgetSize) -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMove: (offset: Int) -> Unit,
    onRemove: () -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = { onMove(-1) }, enabled = canMoveUp) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.action_move_up))
            }
            IconButton(onClick = { onMove(1) }, enabled = canMoveDown) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.action_move_down))
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
            }
        }
        SizeSelector(selected = size, onSelect = onResize)
        if (!fits) {
            Text(
                text = stringResource(R.string.editor_widget_no_room),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            return@Column
        }
        HostedWidgetView(appWidgetId = appWidgetId, manager = manager, modifier = previewModifier(size))
    }
}

private fun previewModifier(size: WidgetSize): Modifier = when (size) {
    WidgetSize.ONE_BY_ONE -> Modifier.fillMaxWidth(0.5f).aspectRatio(1.1f)
    WidgetSize.ONE_BY_TWO -> Modifier.fillMaxWidth().aspectRatio(2.2f)
    WidgetSize.TWO_BY_TWO -> Modifier.fillMaxWidth().aspectRatio(1.1f)
}
