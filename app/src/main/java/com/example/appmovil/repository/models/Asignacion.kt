package com.example.appmovil.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.appmovil.repository.models.Sucursal
import com.example.appmovil.repository.models.Usuario

// Asignación de una sucursal a un técnico (CasoA pantalla 3: Home del técnico).
// El técnico solo ve las empresas y sucursales que tiene asignadas; lo que el
// administrador todavía no asigna es visible únicamente para el propio admin.
@Entity(
    tableName = "asignaciones",
    primaryKeys = ["usuarioId", "sucursalId"],
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Sucursal::class,
            parentColumns = ["id"],
            childColumns = ["sucursalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId"), Index("sucursalId")]
)
data class Asignacion(
    val usuarioId: Long,
    val sucursalId: Long
)
