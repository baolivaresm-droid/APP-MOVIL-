package com.example.appmovil.data.local.empresa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Patrón Ej.8 UserDao: Flow reactivo + suspend para escritura.
@Dao
interface EmpresaDao {
    @Query("SELECT * FROM empresas ORDER BY id DESC")
    fun getEmpresas(): Flow<List<EmpresaEntity>>

    @Query("SELECT * FROM empresas WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EmpresaEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(empresa: EmpresaEntity): Long

    @Query("DELETE FROM empresas WHERE id = :id")
    suspend fun deleteById(id: Long)
}
