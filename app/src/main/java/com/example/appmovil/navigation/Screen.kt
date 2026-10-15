package com.example.appmovil.navigation

// Patrón Ej.3 Navega tres Pantallas: Screen sellada sin "strings mágicos".
// Delega en Route (Ej.8) para que ambos sistemas usen las mismas rutas.
sealed class Screen(val route: String) {
    data object Login : Screen(route = Route.Login.path)
    data object Register : Screen(route = Route.Register.path)
    data object Home : Screen(route = Route.Home.path)
    data object Perfil : Screen(route = Route.Perfil.path)
    data class EmpresaDetalle(val empresaId: Long) : Screen(route = Route.EmpresaDetalle.buildRoute(empresaId))
    data class MedicionForm(val maquinaId: Long) : Screen(route = Route.MedicionForm.buildRoute(maquinaId))
    data class Informe(val medicionId: Long) : Screen(route = Route.Informe.buildRoute(medicionId))
}
