package com.example.appmovil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.repository.data.AppDatabase
import com.example.appmovil.repository.models.Asignacion
import com.example.appmovil.repository.models.Empresa
import com.example.appmovil.repository.models.Sucursal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Controla qué puede ver cada técnico: las empresas que tiene asignadas y las
// sucursales de cada una de esas empresas.
@OptIn(ExperimentalCoroutinesApi::class)
class AsignacionViewModel(application: Application) : AndroidViewModel(application) {

    private val asignacionDao = AppDatabase.getDatabase(application).asignacionDao()

    private val _usuarioId = MutableStateFlow(0L)
    private val _empresaId = MutableStateFlow(0L)

    // Empresas asignadas al técnico: es la lista del Home
    val empresas: StateFlow<List<Empresa>> = _usuarioId
        .flatMapLatest { id ->
            if (id <= 0L) flowOf(emptyList()) else asignacionDao.getEmpresasDeUsuario(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Empresa que el técnico abrió (para mostrar su nombre y RUT)
    val empresaSeleccionada: StateFlow<Empresa?> =
        combine(empresas, _empresaId) { lista, id -> lista.find { it.id == id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Sucursales de la empresa seleccionada que tiene asignadas ese técnico
    val sucursales: StateFlow<List<Sucursal>> =
        combine(_usuarioId, _empresaId) { usuario, empresa -> usuario to empresa }
            .flatMapLatest { (usuario, empresa) ->
                if (usuario <= 0L || empresa <= 0L) flowOf(emptyList())
                else asignacionDao.getSucursalesDeUsuario(usuario, empresa)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observarTecnico(usuarioId: Long) {
        _usuarioId.value = usuarioId
    }

    fun observarEmpresa(empresaId: Long) {
        _empresaId.value = empresaId
    }

    // El administrador asigna una sucursal a un técnico
    fun asignar(usuarioId: Long, sucursalId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                asignacionDao.insertAsignacion(
                    Asignacion(usuarioId = usuarioId, sucursalId = sucursalId)
                )
            }
        }
    }

    fun desasignar(usuarioId: Long, sucursalId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { asignacionDao.deleteAsignacion(usuarioId, sucursalId) }
        }
    }
}
