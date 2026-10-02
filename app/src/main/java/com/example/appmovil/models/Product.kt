package com.example.appmovil.models

data class Product(
    val id: Int,
    val titulo: String,          // Tipo de equipo probado (ej. Transformador de Potencia)
    val codigoEquipo: String,    // Identificador o código del equipo (ej. TR-220-04)
    val ubicacionCliente: String, // Ubicación / instalación del cliente (ej. Subestación Maipú)
    val resultadosAnteriores: String, // Resultados de pruebas anteriores para comparación
    val descripcion: String,
    val imagenResId: Int
)