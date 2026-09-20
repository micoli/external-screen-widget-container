package org.micoli.coverwidgetcontainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.micoli.coverwidgetcontainer.R
import org.micoli.coverwidgetcontainer.data.Container
import org.micoli.coverwidgetcontainer.data.HostedWidget
import org.micoli.coverwidgetcontainer.host.HostedWidgetManager

private const val RENAME_DEBOUNCE_MS = 400L

@Composable
fun ContainerEditorScreen(
    container: Container,
    library: List<HostedWidget>,
    manager: HostedWidgetManager,
    actions: ContainerEditorActions,
) {
    var name by remember(container.index) { mutableStateOf(container.name) }
    LaunchedEffect(name) {
        if (name == container.name) return@LaunchedEffect
        delay(RENAME_DEBOUNCE_MS)
        actions.onRename(name)
    }

    ScreenScaffold(title = container.name, onBack = actions.onBack) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.editor_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
            itemsIndexed(container.pages) { pageIndex, page ->
                PageCard(
                    pageIndex = pageIndex,
                    page = page,
                    canRemove = container.pages.size > 1,
                    library = library,
                    manager = manager,
                    actions = actions,
                )
            }
            item {
                OutlinedButton(onClick = actions.onAddPage, modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(stringResource(R.string.editor_add_page))
                }
            }
        }
    }
}
