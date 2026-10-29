package com.example.appmovil.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.appmovil.repository.models.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    // READ (reactivo con Flow)
    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun getAllUsuarios(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun getUsuarioById(id: Long): Usuario?

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun getUsuarioByEmail(email: String): Usuario?

    // LOGIN
    @Query("SELECT * FROM usuarios WHERE email = :email AND passwordHash = :password LIMIT 1")
    suspend fun login(email: String, password: String): Usuario?

    // Panel del administrador: técnicos y su estado de aprobación
    @Query("SELECT * FROM usuarios WHERE rol = :rol ORDER BY id DESC")
    fun getUsuariosPorRol(rol: String): Flow<List<Usuario>>

    // CREATE (ABORT: el email no se repite)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUsuario(usuario: Usuario): Long

    // UPDATE
    @Update
    suspend fun updateUsuario(usuario: Usuario)

    // DELETE
    @Query("DELETE FROM usuarios WHERE id = :id")
    suspend fun deleteUsuario(id: Long)

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun countUsuarios(): Int
}
