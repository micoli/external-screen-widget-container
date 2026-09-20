package org.micoli.coverwidgetcontainer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.micoli.coverwidgetcontainer.R
import org.micoli.coverwidgetcontainer.data.HostedWidget

@Composable
fun LibraryPickerDialog(
    library: List<HostedWidget>,
    onPick: (HostedWidget) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.editor_from_library)) },
        text = {
            if (library.isEmpty()) {
                Text(stringResource(R.string.editor_library_empty))
                return@AlertDialog
            }
            LazyColumn {
                items(library, key = { it.appWidgetId }) { widget ->
                    ListItem(
                        modifier = Modifier.clickable { onPick(widget) },
                        headlineContent = { Text(widget.label) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
    )
}
