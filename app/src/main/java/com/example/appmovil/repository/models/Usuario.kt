package com.example.appmovil.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// Roles de usuario de la app Induztek.
// ADMIN es el personal autorizado que gestiona y aprueba los registros.
// TECNICO es el que ejecuta las pruebas en terreno.
const val ROL_ADMIN = "ADMIN"
const val ROL_TECNICO = "TECNICO"

// Estados de aprobación: se usan para las cuentas de los técnicos y también
// para la maquinaria que propone un técnico y debe validar el administrador.
const val ESTADO_PENDIENTE = "PENDIENTE"
const val ESTADO_APROBADO = "APROBADO"
const val ESTADO_RECHAZADO = "RECHAZADO"

// Técnico Induztek.
// rol: ADMIN administra y aprueba registros; TECNICO ejecuta las pruebas en terreno.
// estado: el administrador debe aprobar la cuenta antes de que el técnico pueda iniciar sesión.
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String,
    val email: String,
    val telefono: String = "",
    val passwordHash: String,
    val direccion: String = "", // Zona / base de operaciones asignada
    val fotoUri: String = "", // Foto de cámara o galería
    val rol: String = ROL_TECNICO,
    val estado: String = ESTADO_PENDIENTE
)
