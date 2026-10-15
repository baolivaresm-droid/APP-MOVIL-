package com.example.appmovil.data.local.medicion

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Patrón Ej.8 ProtocoloDao: historial ordenado descendente.
@Dao
interface MedicionDao {
    @Query("SELECT * FROM mediciones WHERE maquinaId = :maquinaId ORDER BY id DESC")
    fun getByMaquina(maquinaId: Long): Flow<List<MedicionEntity>>

    @Query("SELECT * FROM mediciones WHERE maquinaId = :maquinaId ORDER BY id DESC")
    suspend fun getListByMaquina(maquinaId: Long): List<MedicionEntity>

    @Query("SELECT * FROM mediciones WHERE maquinaId = :maquinaId ORDER BY id DESC LIMIT 1")
    suspend fun getLastByMaquina(maquinaId: Long): MedicionEntity?

    @Query("SELECT * FROM mediciones WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MedicionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicion: MedicionEntity): Long

    @Query("DELETE FROM mediciones WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM mediciones WHERE maquinaId = :maquinaId")
    suspend fun deleteByMaquina(maquinaId: Long)
}
