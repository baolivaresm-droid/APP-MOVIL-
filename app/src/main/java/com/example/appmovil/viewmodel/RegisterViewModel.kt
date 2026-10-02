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
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            registerError = "Por favor, completa todos los campos"
            return
        }

        viewModelScope.launch {
            try {
                // Verificamos si el correo ya está registrado
                val existingUser = userRepository.getUserByEmail(email.trim())
                if (existingUser != null) {
                    registerError = "El correo ya se encuentra registrado"
                    return@launch
                }

                // Creamos la entidad de usuario (ajusta los campos según tu UserEntity)
                val newUser = UserEntity(
                    email = email.trim(),
                    nombre = name.trim(),
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