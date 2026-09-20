package org.micoli.coverwidgetcontainer.ui

import android.appwidget.AppWidgetProviderInfo
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ConfigApp(
    viewModel: ConfigViewModel,
    onPickProvider: (AppWidgetProviderInfo, AddTarget?) -> Unit,
) {
    val backStack = remember { mutableStateListOf<Screen>(Screen.ContainerList) }
    val containers by viewModel.containers.collectAsStateWithLifecycle()
    val library by viewModel.library.collectAsStateWithLifecycle()
    val placedIndices by viewModel.placedIndices.collectAsStateWithLifecycle()

    fun push(screen: Screen) = backStack.add(screen)
    fun pop() = backStack.removeAt(backStack.lastIndex)

    BackHandler(enabled = backStack.size > 1) { pop() }

    when (val screen = backStack.last()) {
        Screen.ContainerList -> ContainerListScreen(
            containers = containers,
            placedIndices = placedIndices,
            onOpenContainer = { push(Screen.ContainerEditor(it)) },
            onOpenLibrary = { push(Screen.WidgetLibrary) },
            onOpenHelp = { push(Screen.SetupHelp) },
        )

        is Screen.ContainerEditor -> {
            val container = containers.firstOrNull { it.index == screen.containerIndex } ?: return
            ContainerEditorScreen(
                container = container,
                library = library,
                manager = viewModel.manager,
                actions = ContainerEditorActions(
                    onBack = ::pop,
                    onRename = { viewModel.rename(container.index, it) },
                    onAddPage = { viewModel.addPage(container.index) },
                    onRemovePage = { viewModel.removePage(container.index, it) },
                    onNewWidget = { push(Screen.WidgetPicker(AddTarget(container.index, it))) },
                    onAssignWidget = { page, id -> viewModel.assignWidget(container.index, page, id) },
                    onRemoveWidget = { page, id -> viewModel.unassignWidget(container.index, page, id) },
                    onResizeWidget = { page, id, height -> viewModel.resizeWidget(container.index, page, id, height) },
                    onMoveWidget = { page, id, offset -> viewModel.moveWidget(container.index, page, id, offset) },
                ),
            )
        }

        Screen.WidgetLibrary -> WidgetLibraryScreen(
            library = library,
            usageCount = viewModel::usageCount,
            manager = viewModel.manager,
            onAdd = { push(Screen.WidgetPicker(null)) },
            onDelete = viewModel::deleteFromLibrary,
            onBack = ::pop,
        )

        is Screen.WidgetPicker -> WidgetPickerScreen(
            providers = remember { viewModel.manager.installedProviders() },
            onPick = { info ->
                pop()
                onPickProvider(info, screen.target)
            },
            onBack = ::pop,
        )

        Screen.SetupHelp -> SetupHelpScreen(onBack = ::pop)
    }
}
