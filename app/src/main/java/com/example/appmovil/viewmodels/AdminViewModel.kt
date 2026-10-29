package com.example.appmovil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.repository.data.AppDatabase
import com.example.appmovil.repository.models.Asignacion
import com.example.appmovil.repository.models.ESTADO_APROBADO
import com.example.appmovil.repository.models.ESTADO_PENDIENTE
import com.example.appmovil.repository.models.ESTADO_RECHAZADO
import com.example.appmovil.repository.models.Empresa
import com.example.appmovil.repository.models.Maquina
import com.example.appmovil.repository.models.ROL_TECNICO
import com.example.appmovil.repository.models.Sucursal
import com.example.appmovil.repository.models.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Panel del administrador de Induztek: aprueba cuentas, crea empresas y
// sucursales, y valida la maquinaria que proponen los técnicos.
class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val usuarioDao = db.usuarioDao()
    private val empresaDao = db.empresaDao()
    private val sucursalDao = db.sucursalDao()
    private val maquinaDao = db.maquinaDao()
    private val asignacionDao = db.asignacionDao()

    // ---- Cuentas de técnicos ----
    val tecnicos: StateFlow<List<Usuario>> = usuarioDao.getUsuariosPorRol(ROL_TECNICO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---- Empresas ----
    val empresas: StateFlow<List<Empresa>> = empresaDao.getAllEmpresas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---- Sucursales ----
    val sucursales: StateFlow<List<Sucursal>> = sucursalDao.getAllSucursales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---- Maquinaria pendiente de validación ----
    val maquinasPendientes: StateFlow<List<Maquina>> =
        maquinaDao.getMaquinasPorEstado(ESTADO_PENDIENTE)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Inventario completo de maquinaria
    val maquinas: StateFlow<List<Maquina>> = maquinaDao.getAllMaquinas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---- Asignaciones ----
    val asignaciones: StateFlow<List<Asignacion>> = asignacionDao.getAllAsignaciones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Aprobar el registro de un técnico: habilita su inicio de sesión
    fun aprobar(usuarioId: Long) = cambiarEstadoUsuario(usuarioId, ESTADO_APROBADO)

    fun rechazar(usuarioId: Long) = cambiarEstadoUsuario(usuarioId, ESTADO_RECHAZADO)

    private fun cambiarEstadoUsuario(usuarioId: Long, estado: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val usuario = usuarioDao.getUsuarioById(usuarioId) ?: return@withContext
                usuarioDao.updateUsuario(usuario.copy(estado = estado))
            }
        }
    }

    // ---- Empresas ----
    fun guardarEmpresa(nombre: String, rut: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                empresaDao.insertEmpresa(Empresa(nombre = nombre.trim(), rut = rut.trim()))
            }
        }
    }

    fun eliminarEmpresa(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { empresaDao.deleteEmpresa(id) }
        }
    }

    // Editar una empresa existente: permite corregir el nombre o el RUT.
    fun editarEmpresa(id: Long, nombre: String, rut: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val actual = empresaDao.getEmpresaById(id) ?: return@withContext
                empresaDao.updateEmpresa(actual.copy(nombre = nombre.trim(), rut = rut.trim()))
            }
        }
    }

    // ---- Sucursales ----
    fun guardarSucursal(
        empresaId: Long,
        nombre: String,
        direccion: String,
        comuna: String,
        telefono: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                sucursalDao.insertSucursal(
                    Sucursal(
                        empresaId = empresaId,
                        nombre = nombre.trim(),
                        direccion = direccion.trim(),
                        comuna = comuna.trim(),
                        telefono = telefono.trim()
                    )
                )
            }
        }
    }

    fun eliminarSucursal(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { sucursalDao.deleteSucursal(id) }
        }
    }

    // Editar una sucursal existente: permite corregir nombre, dirección, comuna o teléfono.
    fun editarSucursal(
        sucursalId: Long,
        nombre: String,
        direccion: String,
        comuna: String,
        telefono: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val actual = sucursalDao.getSucursalById(sucursalId) ?: return@withContext
                sucursalDao.updateSucursal(
                    actual.copy(
                        nombre = nombre.trim(),
                        direccion = direccion.trim(),
                        comuna = comuna.trim(),
                        telefono = telefono.trim()
                    )
                )
            }
        }
    }

    // ---- Maquinaria ----
    fun guardarMaquina(
        sucursalId: Long,
        categoria: String,
        codigo: String,
        descripcion: String = "",
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) {
                maquinaDao.insertMaquina(
                    Maquina(
                        sucursalId = sucursalId,
                        categoria = categoria.trim(),
                        codigo = codigo.trim().uppercase(),
                        descripcion = descripcion.trim(),
                        estado = ESTADO_APROBADO
                    )
                )
            }
            if (id < 0L) onError()
        }
    }

    fun aprobarMaquina(id: Long) = cambiarEstadoMaquina(id, ESTADO_APROBADO)

    // Editar una máquina registrada/propuesta: permite corregir categoría o código.
    fun editarMaquina(maquinaId: Long, sucursalId: Long, categoria: String, codigo: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val actual = maquinaDao.getMaquinaById(maquinaId) ?: return@withContext
                maquinaDao.updateMaquina(
                    actual.copy(
                        sucursalId = sucursalId,
                        categoria = categoria.trim(),
                        codigo = codigo.trim().uppercase()
                    )
                )
            }
        }
    }

    fun rechazarMaquina(id: Long) = cambiarEstadoMaquina(id, ESTADO_RECHAZADO)

    private fun cambiarEstadoMaquina(maquinaId: Long, estado: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val maquina = maquinaDao.getMaquinaById(maquinaId) ?: return@withContext
                maquinaDao.updateMaquina(maquina.copy(estado = estado))
            }
        }
    }

    // ---- Asignaciones ----
    fun asignarSucursal(usuarioId: Long, sucursalId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                asignacionDao.insertAsignacion(
                    Asignacion(usuarioId = usuarioId, sucursalId = sucursalId)
                )
            }
        }
    }

    fun desasignarSucursal(usuarioId: Long, sucursalId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { asignacionDao.deleteAsignacion(usuarioId, sucursalId) }
        }
    }

    // Asigna todas las sucursales de una empresa a un técnico
    fun asignarEmpresaCompleta(usuarioId: Long, empresaId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val sucursales = sucursalDao.getSucursalesByEmpresa(empresaId).first()
                sucursales.forEach {
                    asignacionDao.insertAsignacion(
                        Asignacion(usuarioId = usuarioId, sucursalId = it.id)
                    )
                }
            }
        }
    }
}
