package org.micoli.coverwidgetcontainer.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WidgetLibraryRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @After
    fun tearDown() = scope.cancel()

    private fun dataStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope) { folder.root.resolve("test.preferences_pb") }

    private val weather = HostedWidget(1, "weather/Provider", "Weather")
    private val clock = HostedWidget(2, "clock/Provider", "Clock")

    @Test
    fun `starts empty`() = runBlocking {
        assertTrue(WidgetLibraryRepository(dataStore()).widgets.first().isEmpty())
    }

    @Test
    fun `keeps widgets in the order they were added`() = runBlocking {
        val repository = WidgetLibraryRepository(dataStore())
        repository.add(weather)
        repository.add(clock)
        assertEquals(listOf(weather, clock), repository.widgets.first())
    }

    @Test
    fun `removes a widget by id`() = runBlocking {
        val repository = WidgetLibraryRepository(dataStore())
        repository.add(weather)
        repository.add(clock)
        repository.remove(1)
        assertEquals(listOf(clock), repository.widgets.first())
    }

    @Test
    fun `removing an unknown id changes nothing`() = runBlocking {
        val repository = WidgetLibraryRepository(dataStore())
        repository.add(weather)
        repository.remove(99)
        assertEquals(listOf(weather), repository.widgets.first())
    }

    @Test
    fun `the library and the containers do not overwrite each other`() = runBlocking {
        val store = dataStore()
        WidgetLibraryRepository(store).add(weather)
        ContainerRepository(store).update(1) { it.withName("Salon") }
        assertEquals(listOf(weather), WidgetLibraryRepository(store).widgets.first())
        assertEquals("Salon", ContainerRepository(store).get(1).name)
    }
}
