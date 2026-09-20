package org.micoli.coverwidgetcontainer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.micoli.coverwidgetcontainer.data.WidgetSize

@Composable
fun SizeSelector(selected: WidgetSize, onSelect: (WidgetSize) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WidgetSize.entries.forEach { size ->
            FilterChip(
                selected = size == selected,
                onClick = { onSelect(size) },
                label = { Text(size.label) },
            )
        }
    }
}
