package org.micoli.coverwidgetcontainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.micoli.coverwidgetcontainer.data.GridCell
import org.micoli.coverwidgetcontainer.data.PageGrid

private val GAP = 4.dp

// Schematic of the 2x2 page: one rectangle per placed widget, in the order they were added.
@Composable
fun PageGridPreview(labels: List<String>, placement: List<GridCell?>, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth(PREVIEW_WIDTH_FRACTION)
            .aspectRatio(PAGE_ASPECT_RATIO)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(GAP),
    ) {
        val cellWidth = (maxWidth - GAP * (PageGrid.COLUMNS - 1)) / PageGrid.COLUMNS
        val cellHeight = (maxHeight - GAP * (PageGrid.ROWS - 1)) / PageGrid.ROWS
        placement.forEachIndexed { position, cell ->
            cell ?: return@forEachIndexed
            Box(
                modifier = Modifier
                    .offset(x = (cellWidth + GAP) * cell.column, y = (cellHeight + GAP) * cell.row)
                    .size(
                        width = cellWidth * cell.columnSpan + GAP * (cell.columnSpan - 1),
                        height = cellHeight * cell.rowSpan + GAP * (cell.rowSpan - 1),
                    )
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = labels.getOrNull(position).orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(4.dp),
                )
            }
        }
    }
}

private const val PREVIEW_WIDTH_FRACTION = 0.5f
private const val PAGE_ASPECT_RATIO = 1.1f
