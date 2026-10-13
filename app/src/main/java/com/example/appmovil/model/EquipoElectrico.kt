package com.example.appmovil.model

// Equipo eléctrico en terreno.
// Caso Induztek 3.2: tipo, código, ubicación, historial, descripción.
data class EquipoElectrico(
    val id: Int,
    val titulo: String,
    val codigoEquipo: String,
    val ubicacionCliente: String,
    val resultadosAnteriores: String,
    val descripcion: String,
    val imagenResId: Int
)
