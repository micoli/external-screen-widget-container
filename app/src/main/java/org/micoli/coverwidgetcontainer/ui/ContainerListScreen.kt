package org.micoli.coverwidgetcontainer.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.micoli.coverwidgetcontainer.R
import org.micoli.coverwidgetcontainer.data.Container

@Composable
fun ContainerListScreen(
    containers: List<Container>,
    placedIndices: Set<Int>,
    onOpenContainer: (Int) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenHelp: () -> Unit,
) {
    ScreenScaffold(
        title = stringResource(R.string.app_name),
        actions = {
            TextButton(onClick = onOpenLibrary) { Text(stringResource(R.string.action_library)) }
            TextButton(onClick = onOpenHelp) { Text(stringResource(R.string.action_help)) }
        },
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            items(containers, key = { it.index }) { container ->
                ContainerListItem(
                    container = container,
                    isPlaced = container.index in placedIndices,
                    onClick = { onOpenContainer(container.index) },
                )
                HorizontalDivider()
            }
        }
    }
}
