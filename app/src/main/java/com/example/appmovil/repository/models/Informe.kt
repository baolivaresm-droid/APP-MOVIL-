package com.example.appmovil.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.appmovil.repository.models.Empresa
import com.example.appmovil.repository.models.Maquina
import com.example.appmovil.repository.models.Sucursal
import com.example.appmovil.repository.models.Usuario

// Informe generado en terreno (CasoA pantalla 6).
// Cada vez que el técnico emite el protocolo PDF queda registrado acá, para que
// el administrador tenga el historial de lo emitido, ordenado por empresa y
// sucursal. pdfPath apunta al archivo generado en el dispositivo.
@Entity(
    tableName = "informes",
    foreignKeys = [
        ForeignKey(
            entity = Empresa::class,
            parentColumns = ["id"],
            childColumns = ["empresaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Sucursal::class,
            parentColumns = ["id"],
            childColumns = ["sucursalId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Maquina::class,
            parentColumns = ["id"],
            childColumns = ["maquinaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("empresaId"), Index("sucursalId"), Index("usuarioId")]
)
data class Informe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val empresaId: Long,
    val sucursalId: Long,
    val maquinaId: Long,
    val usuarioId: Long,
    val codigo: String,
    val categoria: String,
    val valores: String,
    val observaciones: String = "",
    val precio: Double? = null,
    val fechaHora: String,
    val pdfPath: String
)
