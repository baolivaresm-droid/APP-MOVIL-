package com.example.appmovil.data.repository

import com.example.appmovil.data.local.protocolo.ProtocoloDao
import com.example.appmovil.data.local.protocolo.ProtocoloEntity
import kotlinx.coroutines.flow.Flow

// Repositorio de protocolos en terreno (antes carrito de compras).
class ProtocoloRepository(private val protocoloDao: ProtocoloDao) {
    val protocolos: Flow<List<ProtocoloEntity>> = protocoloDao.getProtocolos()

    suspend fun guardarProtocolo(item: ProtocoloEntity) = protocoloDao.insertProtocolo(item)
    suspend fun actualizarProtocolo(item: ProtocoloEntity) = protocoloDao.updateProtocolo(item)
    suspend fun eliminarProtocolo(id: Long) = protocoloDao.deleteProtocolo(id)
    suspend fun sincronizarSeleccionados(ids: List<Long>) {
        if (ids.isNotEmpty()) protocoloDao.deleteByIds(ids)
    }
}
