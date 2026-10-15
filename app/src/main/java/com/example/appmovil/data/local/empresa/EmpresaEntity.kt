package com.example.appmovil.data.local.empresa

import androidx.room.Entity
import androidx.room.PrimaryKey

// Empresa / ubicación en terreno (CasoA pantalla 3: Home).
// Patrón Ej.8 UserEntity: tabla simple con id autogenerado.
@Entity(tableName = "empresas")
data class EmpresaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String,
    val zona: String
)
