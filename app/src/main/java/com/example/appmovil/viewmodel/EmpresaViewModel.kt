package com.example.appmovil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appmovil.data.local.empresa.EmpresaEntity
import com.example.appmovil.data.repository.EmpresaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Patrón Ej.7 ProtocoloViewModel: expone Flow como StateFlow y opera con viewModelScope.
class EmpresaViewModel(private val repository: EmpresaRepository) : ViewModel() {

    val empresas: StateFlow<List<EmpresaEntity>> = repository.empresas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardarEmpresa(nombre: String, zona: String, onGuardada: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.guardar(nombre, zona)
            onGuardada(id)
        }
    }

    fun eliminarEmpresa(id: Long) {
        viewModelScope.launch { repository.eliminar(id) }
    }
}

// Patrón Ej.7 ViewModelFactory: inyecta el repositorio al ViewModel.
class EmpresaViewModelFactory(
    private val repository: EmpresaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmpresaViewModel::class.java)) {
            return EmpresaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
