package com.example.appmovil.navigation

// Patrón Ej.3: eventos de navegación emitidos por el ViewModel.
sealed class NavigationEvent {
    data class NavigateTo(
        val screen: Screen,
        val popUpTo: Screen? = null,
        val inclusive: Boolean = false,
        val singleTop: Boolean = true,
        val clearBackStack: Boolean = false
    ) : NavigationEvent()

    data object PopBackStack : NavigationEvent()
    data object NavigateUp : NavigationEvent()
}
