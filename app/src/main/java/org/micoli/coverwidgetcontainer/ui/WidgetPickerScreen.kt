package org.micoli.coverwidgetcontainer.ui

import android.appwidget.AppWidgetProviderInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.micoli.coverwidgetcontainer.R

private data class LabeledProvider(val info: AppWidgetProviderInfo, val label: String)

private data class AppGroup(val packageName: String, val appLabel: String, val widgets: List<LabeledProvider>)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetPickerScreen(
    providers: List<AppWidgetProviderInfo>,
    onPick: (AppWidgetProviderInfo) -> Unit,
    onBack: () -> Unit,
) {
    val packageManager = LocalContext.current.packageManager
    val groups = remember(providers) { groupByApplication(providers, packageManager) }
    var query by rememberSaveable { mutableStateOf("") }
    val visibleGroups = remember(groups, query) { filterGroups(groups, query) }

    ScreenScaffold(title = stringResource(R.string.picker_title), onBack = onBack) { padding ->
        Column(Modifier.padding(padding)) {
            PickerSearchField(query = query, onQueryChange = { query = it })
            LazyColumn {
                visibleGroups.forEach { group ->
                    stickyHeader(key = "header:${group.packageName}") {
                        PickerGroupHeader(group.appLabel)
                    }
                    items(group.widgets, key = { it.info.provider.flattenToString() }) { (info, label) ->
                        PickerWidgetRow(info = info, label = label, onClick = { onPick(info) })
                    }
                }
            }
        }
    }
}

private fun groupByApplication(
    providers: List<AppWidgetProviderInfo>,
    packageManager: PackageManager,
): List<AppGroup> =
    providers
        .groupBy { it.provider.packageName }
        .map { (packageName, infos) ->
            AppGroup(
                packageName = packageName,
                appLabel = loadApplicationLabel(packageManager, packageName),
                widgets = infos
                    .map { LabeledProvider(it, it.loadLabel(packageManager)) }
                    .sortedBy { it.label.lowercase() },
            )
        }
        .sortedBy { it.appLabel.lowercase() }

private fun loadApplicationLabel(packageManager: PackageManager, packageName: String): String =
    try {
        packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        packageName
    }

private fun filterGroups(groups: List<AppGroup>, query: String): List<AppGroup> {
    val needle = query.trim()
    if (needle.isEmpty()) return groups
    return groups.mapNotNull { group ->
        if (group.appLabel.contains(needle, ignoreCase = true)) return@mapNotNull group
        val matching = group.widgets.filter { it.label.contains(needle, ignoreCase = true) }
        if (matching.isEmpty()) null else group.copy(widgets = matching)
    }
}
