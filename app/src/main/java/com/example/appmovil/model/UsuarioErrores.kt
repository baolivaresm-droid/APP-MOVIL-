package com.example.appmovil.model

// Patrón Ej.4: errores por campo del formulario del profesor.
data class UsuarioErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val clave: String? = null,
    val direccion: String? = null
)
