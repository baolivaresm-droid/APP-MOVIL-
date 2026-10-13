package com.example.appmovil.data.local.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Patrón Ej.8 UserPreferences: sesión en DataStore.
// (El tema claro/oscuro vive en EstadoDataStore + EstadoViewModel, patrón Ej.5.)
val Context.dataStore by preferencesDataStore("induztek_prefs")

class UserPreferences(private val context: Context) {
    private val isLoggedInKey = booleanPreferencesKey("is_logged_key")

    suspend fun setLoggedIn(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[isLoggedInKey] = value }
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[isLoggedInKey] ?: false }
}
