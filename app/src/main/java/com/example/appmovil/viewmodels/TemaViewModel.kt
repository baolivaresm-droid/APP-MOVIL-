package com.example.appmovil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.repository.data.AppDatabase
import com.example.appmovil.view.utils.EstadoDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Tema claro / oscuro persistido en DataStore
class TemaViewModel(application: Application) : AndroidViewModel(application) {

    private val store = EstadoDataStore(application)

    private val _activo = MutableStateFlow<Boolean?>(null)
    val activo: StateFlow<Boolean?> = _activo.asStateFlow()

    init {
        cargarEstado()
    }

    private fun cargarEstado() {
        viewModelScope.launch {
            _activo.value = store.obtenerEstado().first() ?: false
        }
    }

    fun alternarEstado() {
        val nuevo = !(_activo.value ?: false)
        viewModelScope.launch {
            store.guardarEstado(nuevo)
            _activo.value = nuevo
        }
    }
}
