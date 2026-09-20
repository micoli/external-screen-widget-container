package org.micoli.coverwidgetcontainer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString

class ContainerRepository(private val dataStore: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.appDataStore)

    val containers: Flow<List<Container>> = dataStore.data.map(::decode)

    fun container(index: Int): Flow<Container> = containers.map { list -> list.first { it.index == index } }

    suspend fun get(index: Int): Container = container(index).first()

    suspend fun update(index: Int, transform: (Container) -> Container) {
        updateAll { list -> list.map { if (it.index == index) transform(it) else it } }
    }

    suspend fun updateAll(transform: (List<Container>) -> List<Container>) {
        dataStore.edit { prefs ->
            prefs[KEY] = appJson.encodeToString(transform(decode(prefs)))
        }
    }

    private fun decode(prefs: Preferences): List<Container> {
        val stored = prefs[KEY]?.let { appJson.decodeFromString<List<Container>>(it) }.orEmpty()
        return Container.ensureAll(stored)
    }

    private companion object {
        val KEY = stringPreferencesKey("containers")
    }
}
