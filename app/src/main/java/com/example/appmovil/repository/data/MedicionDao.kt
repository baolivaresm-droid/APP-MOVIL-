package com.example.appmovil.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.appmovil.repository.models.Medicion
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicionDao {
    @Query("SELECT * FROM mediciones WHERE maquinaId = :maquinaId ORDER BY id DESC")
    fun getMedicionesByMaquina(maquinaId: Long): Flow<List<Medicion>>

    @Query("SELECT * FROM mediciones WHERE maquinaId = :maquinaId ORDER BY id DESC")
    suspend fun getListaMedicionesByMaquina(maquinaId: Long): List<Medicion>

    @Query("SELECT * FROM mediciones WHERE id = :id")
    suspend fun getMedicionById(id: Long): Medicion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicion(medicion: Medicion): Long

    @Update
    suspend fun updateMedicion(medicion: Medicion)
}
