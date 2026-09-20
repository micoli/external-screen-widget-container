package org.micoli.coverwidgetcontainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.micoli.coverwidgetcontainer.R
import org.micoli.coverwidgetcontainer.data.HostedWidget
import org.micoli.coverwidgetcontainer.data.Page
import org.micoli.coverwidgetcontainer.host.HostedWidgetManager

@Composable
fun PageCard(
    pageIndex: Int,
    page: Page,
    canRemove: Boolean,
    library: List<HostedWidget>,
    manager: HostedWidgetManager,
    actions: ContainerEditorActions,
) {
    var showLibrary by remember { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.editor_page_title, pageIndex + 1),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (canRemove) {
                    TextButton(onClick = { actions.onRemovePage(pageIndex) }) {
                        Text(stringResource(R.string.editor_remove_page))
                    }
                }
            }
            if (page.widgetIds.isEmpty()) {
                Text(stringResource(R.string.editor_page_empty), style = MaterialTheme.typography.bodyMedium)
            }
            val labels = page.widgetIds.map { id -> library.firstOrNull { it.appWidgetId == id }?.label ?: "#$id" }
            val placement = page.placement()
            if (page.widgetIds.isNotEmpty()) PageGridPreview(labels, placement)
            page.widgetIds.forEachIndexed { position, appWidgetId ->
                EditorWidgetRow(
                    label = labels[position],
                    appWidgetId = appWidgetId,
                    manager = manager,
                    size = page.sizeOf(appWidgetId),
                    fits = placement[position] != null,
                    onResize = { size -> actions.onResizeWidget(pageIndex, appWidgetId, size) },
                    canMoveUp = position > 0,
                    canMoveDown = position < page.widgetIds.lastIndex,
                    onMove = { offset -> actions.onMoveWidget(pageIndex, appWidgetId, offset) },
                    onRemove = { actions.onRemoveWidget(pageIndex, appWidgetId) },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { actions.onNewWidget(pageIndex) }) {
                    Text(stringResource(R.string.editor_new_widget))
                }
                OutlinedButton(onClick = { showLibrary = true }) {
                    Text(stringResource(R.string.editor_from_library))
                }
            }
        }
    }

    if (showLibrary) {
        LibraryPickerDialog(
            library = library,
            onPick = { widget ->
                actions.onAssignWidget(pageIndex, widget.appWidgetId)
                showLibrary = false
            },
            onDismiss = { showLibrary = false },
        )
    }
}
