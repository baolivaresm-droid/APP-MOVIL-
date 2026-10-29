package com.example.appmovil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.repository.data.AppDatabase
import com.example.appmovil.repository.models.Medicion
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
class MedicionViewModel(application: Application) : AndroidViewModel(application) {

    private val medicionDao = AppDatabase.getDatabase(application).medicionDao()

    private val _maquinaId = MutableStateFlow(0L)

    // Historial de la máquina seleccionada
    val mediciones: StateFlow<List<Medicion>> = _maquinaId
        .flatMapLatest { id ->
            if (id <= 0L) flowOf(emptyList()) else medicionDao.getMedicionesByMaquina(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Foto de evidencia tomada en terreno. Vive solo en memoria: la app no
    // guarda las fotos de los equipos, se usan para el protocolo PDF y después
    // se descartan.
    private val _fotoEvidencia = MutableStateFlow<String?>(null)
    val fotoEvidencia: StateFlow<String?> = _fotoEvidencia.asStateFlow()

    fun observarMaquina(maquinaId: Long) {
        _maquinaId.value = maquinaId
    }

    fun setFotoEvidencia(uri: String?) {
        _fotoEvidencia.value = uri
    }

    fun guardarMedicion(
        maquinaId: Long,
        valores: String,
        observaciones: String,
        fechaHora: String,
        precio: Double? = null,
        onGuardada: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) {
                medicionDao.insertMedicion(
                    Medicion(
                        maquinaId = maquinaId,
                        valores = valores.trim(),
                        observaciones = observaciones.trim(),
                        fechaHora = fechaHora,
                        precio = precio
                    )
                )
            }
            onGuardada(id)
        }
    }

    // Edición de la medición vigente: permite corregir el valor registrado por
    // error por el técnico antes de que la prueba quede consolidada como histórica.
    fun editarMedicion(
        medicionId: Long,
        valores: String,
        observaciones: String,
        fechaHora: String,
        precio: Double?
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val actual = medicionDao.getMedicionById(medicionId) ?: return@withContext
                medicionDao.updateMedicion(
                    actual.copy(
                        valores = valores.trim(),
                        observaciones = observaciones.trim(),
                        fechaHora = fechaHora,
                        precio = precio
                    )
                )
            }
        }
    }

    // Lecturas puntuales para el informe del protocolo
    suspend fun getMedicion(id: Long): Medicion? = withContext(Dispatchers.IO) {
        medicionDao.getMedicionById(id)
    }

    suspend fun historialDe(maquinaId: Long): List<Medicion> = withContext(Dispatchers.IO) {
        medicionDao.getListaMedicionesByMaquina(maquinaId)
    }
}
