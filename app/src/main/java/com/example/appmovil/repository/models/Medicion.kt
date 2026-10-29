package com.example.appmovil.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.appmovil.repository.models.Maquina

// Medición de una máquina (CasoA pantalla 5: formulario + historial).
// Cada fila es una prueba con fecha/hora; la última es la "actual" y la
// anterior es la "histórica" para la tabla comparativa del informe.
// precio: valor del trabajo ejecutado (opcional), se incluye en el protocolo PDF.
@Entity(
    tableName = "mediciones",
    foreignKeys = [
        ForeignKey(
            entity = Maquina::class,
            parentColumns = ["id"],
            childColumns = ["maquinaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("maquinaId")]
)
data class Medicion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val maquinaId: Long,
    val valores: String,
    val observaciones: String = "",
    val fechaHora: String,
    val precio: Double? = null
)
