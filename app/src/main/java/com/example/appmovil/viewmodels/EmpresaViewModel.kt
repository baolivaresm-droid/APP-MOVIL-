package com.example.appmovil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.repository.data.AppDatabase
import com.example.appmovil.repository.models.Empresa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class EmpresaViewModel(application: Application) : AndroidViewModel(application) {

    private val empresaDao = AppDatabase.getDatabase(application).empresaDao()

    // READ: lista reactiva de empresas cliente
    val empresas: StateFlow<List<Empresa>> = empresaDao.getAllEmpresas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lectura puntual para el informe del protocolo
    suspend fun getEmpresa(id: Long): Empresa? = withContext(Dispatchers.IO) {
        empresaDao.getEmpresaById(id)
    }
}
