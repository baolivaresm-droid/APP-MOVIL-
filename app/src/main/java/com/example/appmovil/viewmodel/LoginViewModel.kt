package com.example.appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    // Estados para los campos de texto
    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    // Estado para mensajes de error o éxito en la interfaz
    var loginError by mutableStateOf<String?>(null)
        private set

    var loginSuccess by mutableStateOf(false)
        private set

    fun onEmailChanged(newEmail: String) {
        email = newEmail
        loginError = null
    }

    fun onPasswordChanged(newPassword: String) {
        password = newPassword
        loginError = null
    }

    // Función para validar y procesar el inicio de sesión
    fun login(onSuccess: () -> Unit) {
        // Validaciones básicas de campos vacíos
        if (email.isBlank() || password.isBlank()) {
            loginError = "Por favor, completa todos los campos"
            return
        }

        viewModelScope.launch {
            try {
                // Buscamos el usuario por su correo en la base de datos local (Room)
                val user = userRepository.getUserByEmail(email.trim())

                if (user != null && user.passwordHash == password) {
                    // Credenciales correctas
                    loginSuccess = true
                    loginError = null
                    onSuccess()
                } else {
                    // Credenciales incorrectas
                    loginError = "Correo o contraseña incorrectos"
                }
            } catch (e: Exception) {
                loginError = "Ocurrió un error al iniciar sesión: ${e.localizedMessage}"
            }
        }
    }
}