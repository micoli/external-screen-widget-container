package org.micoli.coverwidgetcontainer.ui

data class AddTarget(val containerIndex: Int, val pageIndex: Int)

sealed interface Screen {
    data object ContainerList : Screen
    data class ContainerEditor(val containerIndex: Int) : Screen
    data object WidgetLibrary : Screen
    data class WidgetPicker(val target: AddTarget?) : Screen
    data object SetupHelp : Screen
}
