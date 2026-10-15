package com.example.appmovil.data.local.maquina

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.appmovil.data.local.empresa.EmpresaEntity

// Maquinaria en planta (CasoA pantalla 4: detalle/historial empresa).
// fotoRefUri: foto de referencia de la familia; se captura en el primer equipo
// de la categoría y se reutiliza en los siguientes.
@Entity(
    tableName = "maquinas",
    foreignKeys = [
        ForeignKey(
            entity = EmpresaEntity::class,
            parentColumns = ["id"],
            childColumns = ["empresaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("empresaId"), Index(value = ["empresaId", "codigo"], unique = true)]
)
data class MaquinaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val empresaId: Long,
    val categoria: String,
    val codigo: String,
    val fotoRefUri: String = "",
    val descripcion: String = ""
)
