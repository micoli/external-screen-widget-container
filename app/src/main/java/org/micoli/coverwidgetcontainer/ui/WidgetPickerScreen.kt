package org.micoli.coverwidgetcontainer.ui

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.micoli.coverwidgetcontainer.R

private val PREVIEW_SIZE = 88.dp

@Composable
fun WidgetPickerScreen(
    providers: List<AppWidgetProviderInfo>,
    onPick: (AppWidgetProviderInfo) -> Unit,
    onBack: () -> Unit,
) {
    val packageManager = LocalContext.current.packageManager
    val labeled = remember(providers) {
        providers.map { it to it.loadLabel(packageManager) }.sortedBy { (_, label) -> label.lowercase() }
    }

    ScreenScaffold(title = stringResource(R.string.picker_title), onBack = onBack) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            items(labeled, key = { (info, _) -> info.provider.flattenToString() }) { (info, label) ->
                ListItem(
                    modifier = Modifier.clickable { onPick(info) },
                    leadingContent = { WidgetPreviewImage(info, Modifier.size(PREVIEW_SIZE)) },
                    headlineContent = { Text(label) },
                    supportingContent = { Text(info.provider.packageName) },
                )
            }
        }
    }
}
