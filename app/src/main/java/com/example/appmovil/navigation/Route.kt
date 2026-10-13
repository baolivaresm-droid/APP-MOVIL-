package com.example.appmovil.navigation

// Clase sellada para rutas: evita "strings mágicos" (patrón Ej.3 Screen.kt / Ej.8 Routes.kt)
sealed class Route(val path: String) {
    data object Login : Route("login")
    data object Register : Route("register")
    data object Home : Route("home")
    data object Protocolos : Route("protocolos")
    data object Perfil : Route("perfil")

    // Detalle con argumento, igual que Ej.3 Detail(itemId)
    data object Detalle : Route("detalle/{equipoId}") {
        const val ARG = "equipoId"
        fun buildRoute(equipoId: Int): String = "detalle/$equipoId"
    }
}
