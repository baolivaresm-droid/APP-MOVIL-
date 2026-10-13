package com.example.appmovil.data.local.protocolo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtocoloDao {
    @Query("SELECT * FROM protocolos ORDER BY id DESC")
    fun getProtocolos(): Flow<List<ProtocoloEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocolo(item: ProtocoloEntity)

    @Update
    suspend fun updateProtocolo(item: ProtocoloEntity)

    @Query("DELETE FROM protocolos WHERE id = :id")
    suspend fun deleteProtocolo(id: Long)

    @Query("DELETE FROM protocolos")
    suspend fun clearProtocolos()
}
