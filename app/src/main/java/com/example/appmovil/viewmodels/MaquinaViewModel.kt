package com.example.appmovil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.repository.data.AppDatabase
import com.example.appmovil.repository.models.ESTADO_APROBADO
import com.example.appmovil.repository.models.Maquina
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class MaquinaViewModel(application: Application) : AndroidViewModel(application) {

    private val maquinaDao = AppDatabase.getDatabase(application).maquinaDao()

    private val _sucursalId = MutableStateFlow(0L)

    // Maquinaria de la sucursal que se está revisando
    val maquinas: StateFlow<List<Maquina>> = _sucursalId
        .flatMapLatest { id ->
            if (id <= 0L) flowOf(emptyList()) else maquinaDao.getMaquinasBySucursal(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mensaje de error para la vista (ej: código repetido en la sucursal)
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun clearMensaje() { _mensaje.value = null }

    fun observarSucursal(sucursalId: Long) {
        _sucursalId.value = sucursalId
    }

    // Alta de maquinaria. Si la propone un técnico queda PENDIENTE;
    // si la crea el administrador se guarda directamente APROBADA.
    fun guardarMaquina(
        sucursalId: Long,
        categoria: String,
        codigo: String,
        descripcion: String = "",
        creadoPor: Long = 0L,
        estado: String = ESTADO_APROBADO,
        onGuardada: (Long) -> Unit = {},
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
                        estado = estado,
                        creadoPor = creadoPor
                    )
                )
            }
            if (id < 0L) {
                _mensaje.value = "Ya existe una máquina con el código ${codigo.uppercase()} en esta sucursal"
                onError()
            } else {
                onGuardada(id)
            }
        }
    }

    // Lectura puntual para el informe del protocolo
    suspend fun getMaquina(id: Long): Maquina? = withContext(Dispatchers.IO) {
        maquinaDao.getMaquinaById(id)
    }
}
