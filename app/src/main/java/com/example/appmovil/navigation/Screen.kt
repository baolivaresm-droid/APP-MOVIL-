package com.example.appmovil.navigation

// Patrón Ej.3 Navega tres Pantallas: Screen sellada sin "strings mágicos".
// Delega en Route (Ej.8) para que ambos sistemas usen las mismas rutas.
sealed class Screen(val route: String) {
    data object Login : Screen(route = Route.Login.path)
    data object Register : Screen(route = Route.Register.path)
    data object Home : Screen(route = Route.Home.path)
    data object Protocolos : Screen(route = Route.Protocolos.path)
    data object Perfil : Screen(route = Route.Perfil.path)
    data class Detalle(val equipoId: Int) : Screen(route = Route.Detalle.buildRoute(equipoId))
}
