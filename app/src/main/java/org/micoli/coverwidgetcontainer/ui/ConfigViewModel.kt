package org.micoli.coverwidgetcontainer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.micoli.coverwidgetcontainer.cover.ContainerWidgetUpdater
import org.micoli.coverwidgetcontainer.data.Container
import org.micoli.coverwidgetcontainer.data.ContainerIndex
import org.micoli.coverwidgetcontainer.data.ContainerRepository
import org.micoli.coverwidgetcontainer.data.HostedWidget
import org.micoli.coverwidgetcontainer.data.WidgetLibraryRepository
import org.micoli.coverwidgetcontainer.host.HostedWidgetManager

class ConfigViewModel(application: Application) : AndroidViewModel(application) {
    private val containerRepository = ContainerRepository(application)
    private val libraryRepository = WidgetLibraryRepository(application)

    val manager: HostedWidgetManager = HostedWidgetManager.get(application)

    val containers: StateFlow<List<Container>> = containerRepository.containers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val library: StateFlow<List<HostedWidget>> = libraryRepository.widgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val _placedIndices = MutableStateFlow<Set<Int>>(emptySet())
    val placedIndices: StateFlow<Set<Int>> = _placedIndices

    fun refreshPlaced() {
        val context = getApplication<Application>()
        _placedIndices.value = (1..ContainerIndex.COUNT).filter { ContainerWidgetUpdater.isPlaced(context, it) }.toSet()
    }

    fun rename(index: Int, name: String) = mutate(index) { it.withName(name) }

    fun addPage(index: Int) = mutate(index) { it.withPageAdded() }

    fun removePage(index: Int, pageIndex: Int) = mutate(index) { it.withPageRemoved(pageIndex) }

    fun assignWidget(index: Int, pageIndex: Int, appWidgetId: Int) =
        mutate(index) { it.withWidgetAdded(pageIndex, appWidgetId) }

    fun unassignWidget(index: Int, pageIndex: Int, appWidgetId: Int) =
        mutate(index) { it.withWidgetRemoved(pageIndex, appWidgetId) }

    fun resizeWidget(index: Int, pageIndex: Int, appWidgetId: Int, height: Float) =
        mutate(index) { it.withWidgetHeight(pageIndex, appWidgetId, height) }

    fun moveWidget(index: Int, pageIndex: Int, appWidgetId: Int, offset: Int) =
        mutate(index) { it.withWidgetMoved(pageIndex, appWidgetId, offset) }

    fun addToLibrary(widget: HostedWidget, target: AddTarget?) {
        viewModelScope.launch {
            libraryRepository.add(widget)
            target ?: return@launch
            containerRepository.update(target.containerIndex) { it.withWidgetAdded(target.pageIndex, widget.appWidgetId) }
            ContainerWidgetUpdater.refresh(getApplication(), target.containerIndex)
        }
    }

    fun deleteFromLibrary(appWidgetId: Int) {
        viewModelScope.launch {
            containerRepository.updateAll { list -> list.map { it.withoutWidget(appWidgetId) } }
            libraryRepository.remove(appWidgetId)
            manager.delete(appWidgetId)
            ContainerWidgetUpdater.refreshAll(getApplication())
        }
    }

    fun usageCount(appWidgetId: Int): Int =
        containers.value.sumOf { container -> container.pages.count { appWidgetId in it.widgetIds } }

    private fun mutate(index: Int, transform: (Container) -> Container) {
        viewModelScope.launch {
            containerRepository.update(index, transform)
            ContainerWidgetUpdater.refresh(getApplication(), index)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
