package com.example.appmovil.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

// Técnico Induztek. Patrón Ej.8 UserEntity + campos de perfil ya usados (direccion/fotoUri).
@Entity(tableName = "usuarios")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String,
    val email: String,
    val telefono: String = "",
    val passwordHash: String,
    val direccion: String = "", // Zona / base de operaciones asignada
    val fotoUri: String = "" // Foto de cámara o galería
)
