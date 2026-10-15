package com.example.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.local.maquina.MaquinaEntity
import com.example.appmovil.data.repository.MaquinaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Patrón Ej.7 ProtocoloViewModel adaptado a maestro-detalle por empresa.
@OptIn(ExperimentalCoroutinesApi::class)
class MaquinaViewModel(private val repository: MaquinaRepository) : ViewModel() {

    private val _empresaId = MutableStateFlow<Long?>(null)

    val maquinas: StateFlow<List<MaquinaEntity>> = _empresaId
        .flatMapLatest { id ->
            if (id == null || id <= 0L) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                repository.porEmpresa(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observarEmpresa(empresaId: Long) {
        _empresaId.value = empresaId
    }

    fun guardarMaquina(
        empresaId: Long,
        categoria: String,
        codigo: String,
        fotoRefUri: String,
        descripcion: String = "",
        onGuardada: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            // Si ya existe una máquina de la categoría con foto, reutilizarla automáticamente.
            val primera = repository.primeraDeCategoria(empresaId, categoria.trim())
            val fotoFinal = fotoRefUri.ifBlank { primera?.fotoRefUri.orEmpty() }
            val id = repository.guardar(empresaId, categoria, codigo, fotoFinal, descripcion)
            onGuardada(id)
        }
    }

    fun completarFotoRef(maquina: MaquinaEntity, fotoRefUri: String) {
        viewModelScope.launch { repository.actualizarFotoRef(maquina, fotoRefUri) }
    }

    fun eliminarMaquina(id: Long) {
        viewModelScope.launch { repository.eliminar(id) }
    }

    // Lectura puntual para el informe (CasoA pantalla 6).
    suspend fun getById(id: Long) = repository.getById(id)
}

class MaquinaViewModelFactory(
    private val repository: MaquinaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MaquinaViewModel::class.java)) {
            return MaquinaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
