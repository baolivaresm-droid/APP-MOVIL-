package com.example.appmovil.data.local.maquina

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Patrón Ej.8 ProtocoloDao: Flow por padre + escritura suspend.
@Dao
interface MaquinaDao {
    @Query("SELECT * FROM maquinas WHERE empresaId = :empresaId ORDER BY categoria, codigo")
    fun getByEmpresa(empresaId: Long): Flow<List<MaquinaEntity>>

    @Query("SELECT * FROM maquinas WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MaquinaEntity?

    @Query("SELECT * FROM maquinas WHERE empresaId = :empresaId AND categoria = :categoria ORDER BY codigo LIMIT 1")
    suspend fun getFirstOfCategoria(empresaId: Long, categoria: String): MaquinaEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(maquina: MaquinaEntity): Long

    @Update
    suspend fun update(maquina: MaquinaEntity)

    @Query("DELETE FROM maquinas WHERE id = :id")
    suspend fun deleteById(id: Long)
}
