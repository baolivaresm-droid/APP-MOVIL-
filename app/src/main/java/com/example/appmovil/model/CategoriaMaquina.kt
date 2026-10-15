package com.example.appmovil.model

import com.example.appmovil.R

// Catálogo de categorías de maquinaria (CasoA pantalla 4).
// Cada categoría comparte una foto de referencia drawable como respaldo
// cuando la máquina aún no tiene fotoRefUri capturada en terreno.
data class CategoriaMaquina(
    val nombre: String,
    val imagenResId: Int
)

val categoriasMaquina = listOf(
    CategoriaMaquina("Transformadores", R.drawable.transformador),
    CategoriaMaquina("Relés", R.drawable.rele),
    CategoriaMaquina("Celdas", R.drawable.celda),
    CategoriaMaquina("Baterías", R.drawable.baterias)
)

fun imagenDeCategoria(categoria: String): Int =
    categoriasMaquina.find { it.nombre.equals(categoria, ignoreCase = true) }?.imagenResId
        ?: R.drawable.transformador
