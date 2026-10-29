package com.example.appmovil.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.appmovil.repository.models.Maquina
import kotlinx.coroutines.flow.Flow

@Dao
interface MaquinaDao {
    @Query("SELECT * FROM maquinas WHERE sucursalId = :sucursalId ORDER BY categoria, codigo")
    fun getMaquinasBySucursal(sucursalId: Long): Flow<List<Maquina>>

    @Query("SELECT * FROM maquinas WHERE id = :id")
    suspend fun getMaquinaById(id: Long): Maquina?

    // Maquinaria propuesta por los técnicos, a la espera del administrador
    @Query("SELECT * FROM maquinas WHERE estado = :estado ORDER BY id DESC")
    fun getMaquinasPorEstado(estado: String): Flow<List<Maquina>>

    @Query("SELECT * FROM maquinas ORDER BY sucursalId, categoria, codigo")
    fun getAllMaquinas(): Flow<List<Maquina>>

    // IGNORE: si el código ya existe en la sucursal devuelve -1 en vez de lanzar excepción
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMaquina(maquina: Maquina): Long

    @Update
    suspend fun updateMaquina(maquina: Maquina)

    @Query("DELETE FROM maquinas WHERE id = :id")
    suspend fun deleteMaquina(id: Long)
}
