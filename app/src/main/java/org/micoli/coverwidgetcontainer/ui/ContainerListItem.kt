package org.micoli.coverwidgetcontainer.ui

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.micoli.coverwidgetcontainer.R
import org.micoli.coverwidgetcontainer.data.Container

@Composable
fun ContainerListItem(container: Container, isPlaced: Boolean, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(container.name) },
        supportingContent = {
            Text(stringResource(R.string.container_summary, container.pages.size, container.widgetCount))
        },
        trailingContent = {
            if (!isPlaced) return@ListItem
            Text(
                text = stringResource(R.string.container_on_cover),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )
        },
    )
}
