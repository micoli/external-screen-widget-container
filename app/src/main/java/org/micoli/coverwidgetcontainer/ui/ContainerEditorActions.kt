package org.micoli.coverwidgetcontainer.ui

class ContainerEditorActions(
    val onBack: () -> Unit,
    val onRename: (String) -> Unit,
    val onAddPage: () -> Unit,
    val onRemovePage: (pageIndex: Int) -> Unit,
    val onNewWidget: (pageIndex: Int) -> Unit,
    val onAssignWidget: (pageIndex: Int, appWidgetId: Int) -> Unit,
    val onRemoveWidget: (pageIndex: Int, appWidgetId: Int) -> Unit,
    val onResizeWidget: (pageIndex: Int, appWidgetId: Int, height: Float) -> Unit,
    val onMoveWidget: (pageIndex: Int, appWidgetId: Int, offset: Int) -> Unit,
)
