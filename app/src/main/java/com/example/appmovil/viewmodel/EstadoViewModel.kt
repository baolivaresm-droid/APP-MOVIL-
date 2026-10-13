package com.example.appmovil.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.EstadoDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Patrón Ej.5: EstadoViewModel con DataStore + mensaje de confirmación auto-ocultable.
class EstadoViewModel(application: Application) : AndroidViewModel(application) {

    private val store = EstadoDataStore(application)

    private val _activo = MutableStateFlow<Boolean?>(null)
    val activo: StateFlow<Boolean?> = _activo

    private val _mostrarMensaje = MutableStateFlow(false)
    val mostrarMensaje: StateFlow<Boolean> = _mostrarMensaje

    init {
        cargarEstado()
    }

    private fun cargarEstado() {
        viewModelScope.launch {
            delay(300)
            _activo.value = store.obtenerEstado().first() ?: false
        }
    }

    fun alternarEstado() {
        val nuevo = !(_activo.value ?: false)
        viewModelScope.launch {
            store.guardarEstado(nuevo)
            _activo.value = nuevo
            _mostrarMensaje.value = true
            delay(2000)
            _mostrarMensaje.value = false
        }
    }
}
