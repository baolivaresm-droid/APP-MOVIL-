package com.example.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.local.protocolo.ProtocoloEntity
import com.example.appmovil.data.repository.ProtocoloRepository
import com.example.appmovil.model.EquipoElectrico
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Patrón Ej.7 ProductViewModel: expone Flow como StateFlow y opera con viewModelScope.
class ProtocoloViewModel(private val repository: ProtocoloRepository) : ViewModel() {

    val protocolos: StateFlow<List<ProtocoloEntity>> = repository.protocolos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardarProtocolo(equipo: EquipoElectrico, valoresMedicion: String, fechaHora: String) {
        viewModelScope.launch {
            repository.guardarProtocolo(
                ProtocoloEntity(
                    equipoId = equipo.id,
                    titulo = equipo.titulo,
                    codigoEquipo = equipo.codigoEquipo,
                    ubicacionCliente = equipo.ubicacionCliente,
                    valoresMedicion = valoresMedicion,
                    fechaHora = fechaHora,
                    imagenResId = equipo.imagenResId,
                    isSelected = true
                )
            )
        }
    }

    fun alternarSeleccion(item: ProtocoloEntity) {
        viewModelScope.launch {
            repository.actualizarProtocolo(item.copy(isSelected = !item.isSelected))
        }
    }

    fun eliminarProtocolo(id: Long) {
        viewModelScope.launch { repository.eliminarProtocolo(id) }
    }

    fun sincronizarSeleccionados(items: List<ProtocoloEntity>) {
        if (items.isEmpty()) return
        viewModelScope.launch { repository.sincronizarSeleccionados(items.map { it.id }) }
    }
}

// Patrón Ej.7 ViewModelFactory: inyecta el repositorio al ViewModel.
class ProtocoloViewModelFactory(
    private val repository: ProtocoloRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProtocoloViewModel::class.java)) {
            return ProtocoloViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
