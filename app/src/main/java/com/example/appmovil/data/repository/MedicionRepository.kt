package com.example.appmovil.data.repository

import com.example.appmovil.data.local.medicion.MedicionDao
import com.example.appmovil.data.local.medicion.MedicionEntity
import kotlinx.coroutines.flow.Flow

// Repositorio de mediciones por máquina (CasoA pantallas 5 y 6).
class MedicionRepository(private val dao: MedicionDao) {
    fun porMaquina(maquinaId: Long): Flow<List<MedicionEntity>> = dao.getByMaquina(maquinaId)

    suspend fun getById(id: Long): MedicionEntity? = dao.getById(id)
    suspend fun ultimaDe(maquinaId: Long): MedicionEntity? = dao.getLastByMaquina(maquinaId)
    suspend fun historialDe(maquinaId: Long): List<MedicionEntity> = dao.getListByMaquina(maquinaId)

    suspend fun guardar(
        maquinaId: Long,
        valores: String,
        observaciones: String,
        fechaHora: String
    ): Long = dao.insert(
        MedicionEntity(
            maquinaId = maquinaId,
            valores = valores.trim(),
            observaciones = observaciones.trim(),
            fechaHora = fechaHora
        )
    )

    suspend fun eliminar(id: Long) = dao.deleteById(id)
}
