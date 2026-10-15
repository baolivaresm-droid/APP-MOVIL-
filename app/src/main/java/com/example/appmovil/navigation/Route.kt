package com.example.appmovil.navigation

// Clase sellada para rutas: evita "strings mágicos" (patrón Ej.3 Screen.kt / Ej.8 Routes.kt)
// CasoA 7 pantallas: Login, Registro, Home empresas, Detalle empresa, Formulario medición, Informe PDF, Perfil.
sealed class Route(val path: String) {
    data object Login : Route("login")
    data object Register : Route("register")
    data object Home : Route("home")
    data object Perfil : Route("perfil")

    // Detalle de empresa con argumento empresaId
    data object EmpresaDetalle : Route("empresa_detalle/{empresaId}") {
        const val ARG = "empresaId"
        fun buildRoute(empresaId: Long): String = "empresa_detalle/$empresaId"
    }

    // Formulario de medición con argumento maquinaId
    data object MedicionForm : Route("medicion_form/{maquinaId}") {
        const val ARG = "maquinaId"
        fun buildRoute(maquinaId: Long): String = "medicion_form/$maquinaId"
    }

    // Informe con argumento medicionId
    data object Informe : Route("informe/{medicionId}") {
        const val ARG = "medicionId"
        fun buildRoute(medicionId: Long): String = "informe/$medicionId"
    }
}
