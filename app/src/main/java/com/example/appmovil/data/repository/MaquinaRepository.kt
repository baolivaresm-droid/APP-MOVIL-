package com.example.appmovil.data.repository

import com.example.appmovil.data.local.maquina.MaquinaDao
import com.example.appmovil.data.local.maquina.MaquinaEntity
import kotlinx.coroutines.flow.Flow

// Repositorio de maquinaria por empresa (CasoA pantalla 4).
class MaquinaRepository(private val dao: MaquinaDao) {
    fun porEmpresa(empresaId: Long): Flow<List<MaquinaEntity>> = dao.getByEmpresa(empresaId)

    suspend fun getById(id: Long): MaquinaEntity? = dao.getById(id)

    suspend fun primeraDeCategoria(empresaId: Long, categoria: String): MaquinaEntity? =
        dao.getFirstOfCategoria(empresaId, categoria)

    suspend fun guardar(
        empresaId: Long,
        categoria: String,
        codigo: String,
        fotoRefUri: String,
        descripcion: String = ""
    ): Long = dao.insert(
        MaquinaEntity(
            empresaId = empresaId,
            categoria = categoria.trim(),
            codigo = codigo.trim().uppercase(),
            fotoRefUri = fotoRefUri.trim(),
            descripcion = descripcion.trim()
        )
    )

    suspend fun actualizarFotoRef(maquina: MaquinaEntity, fotoRefUri: String) =
        dao.update(maquina.copy(fotoRefUri = fotoRefUri.trim()))

    suspend fun eliminar(id: Long) = dao.deleteById(id)
}
