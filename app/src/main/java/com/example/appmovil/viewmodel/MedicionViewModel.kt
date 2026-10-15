package com.example.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.local.medicion.MedicionEntity
import com.example.appmovil.data.repository.MedicionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Patrón Ej.7 ProtocoloViewModel adaptado al historial por máquina.
@OptIn(ExperimentalCoroutinesApi::class)
class MedicionViewModel(private val repository: MedicionRepository) : ViewModel() {

    private val _maquinaId = MutableStateFlow<Long?>(null)

    val mediciones: StateFlow<List<MedicionEntity>> = _maquinaId
        .flatMapLatest { id ->
            if (id == null || id <= 0L) flowOf(emptyList()) else repository.porMaquina(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observarMaquina(maquinaId: Long) {
        _maquinaId.value = maquinaId
    }

    fun guardarMedicion(
        maquinaId: Long,
        valores: String,
        observaciones: String,
        fechaHora: String,
        onGuardada: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = repository.guardar(maquinaId, valores, observaciones, fechaHora)
            onGuardada(id)
        }
    }

    fun eliminarMedicion(id: Long) {
        viewModelScope.launch { repository.eliminar(id) }
    }

    // Lecturas puntuales para el informe (CasoA pantalla 6).
    suspend fun getById(id: Long) = repository.getById(id)
    suspend fun historialDe(maquinaId: Long) = repository.historialDe(maquinaId)
}

class MedicionViewModelFactory(
    private val repository: MedicionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicionViewModel::class.java)) {
            return MedicionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
