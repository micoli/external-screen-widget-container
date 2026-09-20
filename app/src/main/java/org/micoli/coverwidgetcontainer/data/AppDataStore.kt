package org.micoli.coverwidgetcontainer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.serialization.json.Json

internal val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "cover_widget_container")

internal val appJson = Json { ignoreUnknownKeys = true }
