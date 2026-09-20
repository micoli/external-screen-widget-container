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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ContainerRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @After
    fun tearDown() = scope.cancel()

    private fun dataStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope) { folder.root.resolve("test.preferences_pb") }

    @Test
    fun `starts with the default containers`() = runBlocking {
        val containers = ContainerRepository(dataStore()).containers.first()
        assertEquals(ContainerIndex.COUNT, containers.size)
        assertEquals("Container 1", containers.first().name)
        assertEquals(listOf(Page()), containers.first().pages)
    }

    @Test
    fun `update changes only the targeted container`() = runBlocking {
        val repository = ContainerRepository(dataStore())
        repository.update(2) { it.withName("Salon").withWidgetAdded(0, 90) }

        val containers = repository.containers.first()
        assertEquals("Salon", containers[1].name)
        assertEquals(listOf(90), containers[1].pages[0].widgetIds)
        assertEquals("Container 1", containers[0].name)
        assertEquals("Container 3", containers[2].name)
    }

    @Test
    fun `get returns the stored container`() = runBlocking {
        val repository = ContainerRepository(dataStore())
        repository.update(5) { it.withPageAdded() }
        assertEquals(2, repository.get(5).pages.size)
    }

    @Test
    fun `changes survive a new repository on the same store`() = runBlocking {
        val store = dataStore()
        ContainerRepository(store).update(1) { it.withName("Persisted") }
        assertEquals("Persisted", ContainerRepository(store).get(1).name)
    }

    @Test
    fun `updateAll can rewrite every container`() = runBlocking {
        val repository = ContainerRepository(dataStore())
        repository.updateAll { list -> list.map { it.withoutWidget(9) } }
        assertEquals(ContainerIndex.COUNT, repository.containers.first().size)
    }

    @Test
    fun `sizes are kept per page after a save`() = runBlocking {
        val repository = ContainerRepository(dataStore())
        repository.update(1) { it.withWidgetAdded(0, 7).withWidgetSize(0, 7, WidgetSize.TWO_BY_TWO) }
        assertEquals(WidgetSize.TWO_BY_TWO, repository.get(1).pages[0].sizeOf(7))
    }
}
