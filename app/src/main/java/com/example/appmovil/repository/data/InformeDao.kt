package com.example.appmovil.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appmovil.repository.models.Informe
import kotlinx.coroutines.flow.Flow

@Dao
interface InformeDao {
    @Query("SELECT * FROM informes ORDER BY fechaHora DESC, id DESC")
    fun getAllInformes(): Flow<List<Informe>>

    @Query("SELECT * FROM informes WHERE id = :id")
    suspend fun getInformeById(id: Long): Informe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInforme(informe: Informe): Long
}
