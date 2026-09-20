package org.micoli.coverwidgetcontainer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.micoli.coverwidgetcontainer.R
import org.micoli.coverwidgetcontainer.data.HostedWidget
import org.micoli.coverwidgetcontainer.host.HostedWidgetManager

@Composable
fun WidgetLibraryScreen(
    library: List<HostedWidget>,
    usageCount: (appWidgetId: Int) -> Int,
    manager: HostedWidgetManager,
    onAdd: () -> Unit,
    onDelete: (appWidgetId: Int) -> Unit,
    onBack: () -> Unit,
) {
    ScreenScaffold(
        title = stringResource(R.string.library_title),
        onBack = onBack,
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add))
            }
        },
    ) { padding ->
        if (library.isEmpty()) {
            Text(stringResource(R.string.library_empty), modifier = Modifier.padding(padding).padding(16.dp))
            return@ScreenScaffold
        }
        LazyColumn(Modifier.padding(padding)) {
            items(library, key = { it.appWidgetId }) { widget ->
                Column {
                    ListItem(
                        headlineContent = { Text(widget.label) },
                        supportingContent = { Text(stringResource(R.string.library_usage, usageCount(widget.appWidgetId))) },
                        trailingContent = {
                            IconButton(onClick = { onDelete(widget.appWidgetId) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                            }
                        },
                    )
                    HostedWidgetView(
                        appWidgetId = widget.appWidgetId,
                        manager = manager,
                        modifier = Modifier.fillMaxWidth().height(120.dp).padding(horizontal = 16.dp),
                    )
                    HorizontalDivider(Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}
