package com.example.appmovil.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.appmovil.repository.models.Empresa

// Sucursal / instalación del cliente (CasoA pantalla 3: Home del técnico).
// Es la ubicación exacta donde el técnico ejecuta la prueba: la que exige el
// punto 3.2 del caso ("ubicación/instalación del cliente").
@Entity(
    tableName = "sucursales",
    foreignKeys = [
        ForeignKey(
            entity = Empresa::class,
            parentColumns = ["id"],
            childColumns = ["empresaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("empresaId")]
)
data class Sucursal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val empresaId: Long,
    val nombre: String,
    val direccion: String,
    val comuna: String,
    val telefono: String = ""
)
