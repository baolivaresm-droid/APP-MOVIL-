package com.example.appmovil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.repository.data.AppDatabase
import com.example.appmovil.repository.models.Informe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Historial de protocolos emitidos: el técnico registra el informe al generarlo
// y el administrador lo revisa en su panel.
class InformeViewModel(application: Application) : AndroidViewModel(application) {

    private val informeDao = AppDatabase.getDatabase(application).informeDao()

    val informes: StateFlow<List<Informe>> = informeDao.getAllInformes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun registrar(
        empresaId: Long,
        sucursalId: Long,
        maquinaId: Long,
        usuarioId: Long,
        codigo: String,
        categoria: String,
        valores: String,
        observaciones: String,
        precio: Double?,
        fechaHora: String,
        pdfPath: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                informeDao.insertInforme(
                    Informe(
                        empresaId = empresaId,
                        sucursalId = sucursalId,
                        maquinaId = maquinaId,
                        usuarioId = usuarioId,
                        codigo = codigo,
                        categoria = categoria,
                        valores = valores,
                        observaciones = observaciones,
                        precio = precio,
                        fechaHora = fechaHora,
                        pdfPath = pdfPath
                    )
                )
            }
        }
    }

    suspend fun getInforme(id: Long): Informe? = withContext(Dispatchers.IO) {
        informeDao.getInformeById(id)
    }
}
