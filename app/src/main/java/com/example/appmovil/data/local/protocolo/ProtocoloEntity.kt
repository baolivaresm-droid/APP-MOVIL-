package com.example.appmovil.data.local.protocolo

import androidx.room.Entity
import androidx.room.PrimaryKey

// Protocolo de prueba en terreno (antes CartItemEntity/carrito de tienda).
// Campos exigidos por el caso 3.2: tipo equipo, código, medición, fecha/hora, ubicación, historial.
@Entity(tableName = "protocolos")
data class ProtocoloEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val equipoId: Int, // Antes productId
    val titulo: String, // Tipo de equipo probado
    val codigoEquipo: String,
    val ubicacionCliente: String,
    val valoresMedicion: String,
    val fechaHora: String,
    val imagenResId: Int,
    val isSelected: Boolean = true // Seleccionado para envío/sincronización
)
