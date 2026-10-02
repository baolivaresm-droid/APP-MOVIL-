package com.example.appmovil.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carrito") // Mantenemos el nombre original de la tabla para que coincida con el DAO
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val productId: Int,
    val titulo: String,
    val codigoEquipo: String,
    val ubicacionCliente: String,
    val valoresMedicion: String, // Resultado de la prueba ingresado por el técnico
    val fechaHora: String,       // Fecha y hora del registro
    val imagenResId: Int,
    val cantidad: Int = 1,
    val isSelected: Boolean = true
)