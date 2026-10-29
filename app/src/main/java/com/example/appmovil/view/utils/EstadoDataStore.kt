package com.example.appmovil.view.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Patrón Ej.5 Animacion y dataStore: EstadoDataStore con Preferences.
val Context.estadoDataStore by preferencesDataStore(name = "preferencias_usuario")

class EstadoDataStore(private val context: Context) {

    private val ESTADO_ACTIVADO = booleanPreferencesKey(name = "modo_activado")

    suspend fun guardarEstado(valor: Boolean) {
        context.estadoDataStore.edit { it[ESTADO_ACTIVADO] = valor }
    }

    fun obtenerEstado(): Flow<Boolean?> =
        context.estadoDataStore.data.map { it[ESTADO_ACTIVADO] }
}
