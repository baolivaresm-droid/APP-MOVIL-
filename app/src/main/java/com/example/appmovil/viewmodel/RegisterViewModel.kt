package com.example.appmovil.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.models.UserEntity
import com.example.appmovil.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    // Estados para los campos de registro
    var name by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var registerError by mutableStateOf<String?>(null)
        private set

    var registerSuccess by mutableStateOf(false)
        private set

    fun onNameChanged(newName: String) {
        name = newName
        registerError = null
    }

    fun onEmailChanged(newEmail: String) {
        email = newEmail
        registerError = null
    }

    fun onPasswordChanged(newPassword: String) {
        password = newPassword
        registerError = null
    }

    // Función para registrar al usuario validando que no exista previamente
    fun register(onSuccess: () -> Unit) {
        val cleanName = name.trim()
        val cleanEmail = email.trim()

        // 1. Validar campos vacíos
        if (cleanName.isBlank() || cleanEmail.isBlank() || password.isBlank()) {
            registerError = "Por favor, completa todos los campos"
            return
        }

        // 2. Validar que el nombre tenga al menos 6 caracteres
        if (cleanName.length < 6) {
            registerError = "El nombre debe tener al menos 6 caracteres"
            return
        }

        // 3. Validar que el correo contenga '@'
        if (!cleanEmail.contains("@")) {
            registerError = "El correo debe incluir el símbolo '@'"
            return
        }

        // 4. Validar que el correo tenga al menos 6 caracteres
        if (cleanEmail.length < 6) {
            registerError = "El correo debe tener al menos 6 caracteres"
            return
        }

        // 5. Validar que la contraseña tenga al menos 6 caracteres
        if (password.length < 6) {
            registerError = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        viewModelScope.launch {
            try {
                // Verificamos si el correo ya está registrado en Room
                val existingUser = userRepository.getUserByEmail(cleanEmail)
                if (existingUser != null) {
                    registerError = "El correo ya se encuentra registrado"
                    return@launch
                }

                // Creamos la entidad de usuario
                val newUser = UserEntity(
                    email = cleanEmail,
                    nombre = cleanName,
                    passwordHash = password
                )

                userRepository.registerUser(newUser)
                registerSuccess = true
                registerError = null
                onSuccess()
            } catch (e: Exception) {
                registerError = "Error al registrar usuario: ${e.localizedMessage}"
            }
        }
    }
}