package com.example.appmovil.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.appmovil.repository.models.Sucursal
import kotlinx.coroutines.flow.Flow

@Dao
interface SucursalDao {
    @Query("SELECT * FROM sucursales ORDER BY nombre ASC")
    fun getAllSucursales(): Flow<List<Sucursal>>

    @Query("SELECT * FROM sucursales WHERE empresaId = :empresaId ORDER BY nombre ASC")
    fun getSucursalesByEmpresa(empresaId: Long): Flow<List<Sucursal>>

    @Query("SELECT * FROM sucursales WHERE id = :id")
    suspend fun getSucursalById(id: Long): Sucursal?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSucursal(sucursal: Sucursal): Long

    @Update
    suspend fun updateSucursal(sucursal: Sucursal)

    @Query("DELETE FROM sucursales WHERE id = :id")
    suspend fun deleteSucursal(id: Long)
}
