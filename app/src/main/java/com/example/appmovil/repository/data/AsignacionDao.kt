package com.example.appmovil.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appmovil.repository.models.Asignacion
import com.example.appmovil.repository.models.Empresa
import com.example.appmovil.repository.models.Sucursal
import kotlinx.coroutines.flow.Flow

@Dao
interface AsignacionDao {
    @Query("SELECT * FROM asignaciones")
    fun getAllAsignaciones(): Flow<List<Asignacion>>

    // Empresas que el técnico puede ver: las que tienen al menos una sucursal asignada.
    @Query(
        """
        SELECT DISTINCT e.* FROM empresas e
        INNER JOIN sucursales s ON s.empresaId = e.id
        INNER JOIN asignaciones a ON a.sucursalId = s.id
        WHERE a.usuarioId = :usuarioId
        ORDER BY e.nombre
        """
    )
    fun getEmpresasDeUsuario(usuarioId: Long): Flow<List<Empresa>>

    // Sucursales de una empresa asignadas a ese técnico.
    @Query(
        """
        SELECT s.* FROM sucursales s
        INNER JOIN asignaciones a ON a.sucursalId = s.id
        WHERE a.usuarioId = :usuarioId AND s.empresaId = :empresaId
        ORDER BY s.nombre
        """
    )
    fun getSucursalesDeUsuario(usuarioId: Long, empresaId: Long): Flow<List<Sucursal>>

    @Query("SELECT * FROM asignaciones WHERE usuarioId = :usuarioId")
    fun getAsignacionesPorUsuario(usuarioId: Long): Flow<List<Asignacion>>

    @Query("SELECT * FROM asignaciones WHERE sucursalId = :sucursalId")
    fun getAsignacionesPorSucursal(sucursalId: Long): Flow<List<Asignacion>>

    // IGNORE: si ya estaba asignada no hace nada
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAsignacion(asignacion: Asignacion)

    @Query("DELETE FROM asignaciones WHERE usuarioId = :usuarioId AND sucursalId = :sucursalId")
    suspend fun deleteAsignacion(usuarioId: Long, sucursalId: Long)
}
