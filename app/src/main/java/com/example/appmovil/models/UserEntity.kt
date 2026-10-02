package com.example.appmovil.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val email: String,
    val nombre: String,
    val passwordHash: String,
    val direccion: String = "",
    val fotoUri: String = "" // Ruta local de la foto de la Cámara o Galería
)