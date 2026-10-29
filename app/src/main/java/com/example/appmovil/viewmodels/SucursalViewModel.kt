package com.example.appmovil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.repository.data.AppDatabase
import com.example.appmovil.repository.models.Sucursal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class SucursalViewModel(application: Application) : AndroidViewModel(application) {

    private val sucursalDao = AppDatabase.getDatabase(application).sucursalDao()

    // READ: todas las sucursales de los clientes
    val sucursales: StateFlow<List<Sucursal>> = sucursalDao.getAllSucursales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lectura puntual para el informe del protocolo
    suspend fun getSucursal(id: Long): Sucursal? = withContext(Dispatchers.IO) {
        sucursalDao.getSucursalById(id)
    }
}
