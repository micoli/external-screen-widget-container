package org.micoli.coverwidgetcontainer.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString

class WidgetLibraryRepository(context: Context) {
    private val dataStore = context.applicationContext.appDataStore

    val widgets: Flow<List<HostedWidget>> = dataStore.data.map(::decode)

    suspend fun add(widget: HostedWidget) = edit { it + widget }

    suspend fun remove(appWidgetId: Int) = edit { list -> list.filterNot { it.appWidgetId == appWidgetId } }

    private suspend fun edit(transform: (List<HostedWidget>) -> List<HostedWidget>) {
        dataStore.edit { prefs ->
            prefs[KEY] = appJson.encodeToString(transform(decode(prefs)))
        }
    }

    private fun decode(prefs: Preferences): List<HostedWidget> =
        prefs[KEY]?.let { appJson.decodeFromString<List<HostedWidget>>(it) }.orEmpty()

    private companion object {
        val KEY = stringPreferencesKey("library")
    }
}
