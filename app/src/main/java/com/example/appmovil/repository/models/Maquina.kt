package com.example.appmovil.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.appmovil.repository.models.Sucursal
import com.example.appmovil.repository.models.ESTADO_APROBADO

// Maquinaria en planta (CasoA pantalla 4: detalle/historial de la sucursal).
// La foto de referencia del equipo es la imagen de su categoría (rele.jpg,
// transformador.jpg, etc.): la app no guarda fotos tomadas en terreno.
// estado: el administrador la deja APROBADA; si la propone un técnico queda
// PENDIENTE hasta que la valide.
@Entity(
    tableName = "maquinas",
    foreignKeys = [
        ForeignKey(
            entity = Sucursal::class,
            parentColumns = ["id"],
            childColumns = ["sucursalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sucursalId"), Index(value = ["sucursalId", "codigo"], unique = true)]
)
data class Maquina(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sucursalId: Long,
    val categoria: String,
    val codigo: String,
    val descripcion: String = "",
    val estado: String = ESTADO_APROBADO,
    val creadoPor: Long = 0L
)
