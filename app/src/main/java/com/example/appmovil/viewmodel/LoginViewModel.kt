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
        val cleanEmail = email.trim()

        // 1. Validar campos vacíos
        if (cleanEmail.isBlank() || password.isBlank()) {
            loginError = "Por favor, completa todos los campos"
            return
        }

        // 2. Validar formato de correo (debe contener '@')
        if (!cleanEmail.contains("@")) {
            loginError = "El correo debe ser válido (debe incluir '@')"
            return
        }

        // 3. Validar longitud mínima del correo/usuario (mínimo 6 caracteres)
        if (cleanEmail.length < 6) {
            loginError = "El correo debe tener al menos 6 caracteres"
            return
        }

        // 4. Validar longitud mínima de la contraseña (mínimo 6 caracteres)
        if (password.length < 6) {
            loginError = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        // Si pasa todas las validaciones locales, consulta a la Base de Datos
        viewModelScope.launch {
            try {
                val user = userRepository.getUserByEmail(cleanEmail)

                if (user != null && user.passwordHash == password) {
                    loginSuccess = true
                    loginError = null
                    onSuccess()
                } else {
                    loginError = "Correo o contraseña incorrectos"
                }
            } catch (e: Exception) {
                loginError = "Ocurrió un error al iniciar sesión: ${e.localizedMessage}"
            }
        }
    }
}