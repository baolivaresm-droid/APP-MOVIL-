package com.example.appmovil.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carrito")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val productId: Int,
    val titulo: String,
    val precio: Double,
    val imagenResId: Int, // O String si viene de URL
    val cantidad: Int = 1,
    val isSelected: Boolean = true // Permite seleccionar/deseleccionar elementos en el carrito
)