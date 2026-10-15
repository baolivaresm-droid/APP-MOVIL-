package com.example.appmovil.data.repository

import com.example.appmovil.data.local.empresa.EmpresaDao
import com.example.appmovil.data.local.empresa.EmpresaEntity
import kotlinx.coroutines.flow.Flow

// Repositorio de empresas en terreno (CasoA pantalla 3).
// Patrón Ej.7 ProtocoloRepository: expone Flow + suspend para escritura.
class EmpresaRepository(private val dao: EmpresaDao) {
    val empresas: Flow<List<EmpresaEntity>> = dao.getEmpresas()

    suspend fun getById(id: Long): EmpresaEntity? = dao.getById(id)
    suspend fun guardar(nombre: String, zona: String): Long =
        dao.insert(EmpresaEntity(nombre = nombre.trim(), zona = zona.trim()))
    suspend fun eliminar(id: Long) = dao.deleteById(id)
}
