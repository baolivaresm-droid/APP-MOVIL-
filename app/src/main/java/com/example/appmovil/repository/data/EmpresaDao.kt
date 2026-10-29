package com.example.appmovil.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.appmovil.repository.models.Empresa
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpresaDao {
    @Query("SELECT * FROM empresas ORDER BY id DESC")
    fun getAllEmpresas(): Flow<List<Empresa>>

    @Query("SELECT * FROM empresas WHERE id = :id")
    suspend fun getEmpresaById(id: Long): Empresa?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEmpresa(empresa: Empresa): Long

    @Update
    suspend fun updateEmpresa(empresa: Empresa)

    @Query("DELETE FROM empresas WHERE id = :id")
    suspend fun deleteEmpresa(id: Long)
}
