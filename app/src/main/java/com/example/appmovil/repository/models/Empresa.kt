package com.example.appmovil.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// Empresa cliente de Induztek (CasoA pantalla 3: Home del administrador).
// La empresa agrupa las sucursales donde el técnico ejecuta las pruebas.
@Entity(tableName = "empresas")
data class Empresa(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String,
    val rut: String
)
