package com.example.appmovil.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.repository.data.AppDatabase
import com.example.appmovil.repository.models.ESTADO_PENDIENTE
import com.example.appmovil.repository.models.ROL_TECNICO
import com.example.appmovil.repository.models.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RegistroUiState(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val password: String = "",
    val confirmar: String = "",
    val nombreError: String? = null,
    val emailError: String? = null,
    val telefonoError: String? = null,
    val passwordError: String? = null,
    val confirmarError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

// El registro público solo crea cuentas de técnico, siempre pendientes de aprobación.
class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioDao = AppDatabase.getDatabase(application).usuarioDao()

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun onNombreChange(value: String) {
        val filtrado = value.filter { it.isLetter() || it.isWhitespace() }
        _uiState.update {
            it.copy(nombre = filtrado, nombreError = Validaciones.nombre(filtrado), error = null)
        }
    }

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value, emailError = Validaciones.email(value), error = null)
    }

    fun onTelefonoChange(value: String) {
        val digitos = value.filter { it.isDigit() }
        _uiState.update {
            it.copy(telefono = digitos, telefonoError = Validaciones.telefono(digitos), error = null)
        }
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(
            password = value,
            passwordError = Validaciones.password(value),
            confirmarError = Validaciones.confirmar(value, it.confirmar)
        )
    }

    fun onConfirmarChange(value: String) = _uiState.update {
        it.copy(confirmar = value, confirmarError = Validaciones.confirmar(it.password, value))
    }

    fun registrar() {
        val state = _uiState.value
        val nombreError = Validaciones.nombre(state.nombre)
        val emailError = Validaciones.email(state.email)
        val telefonoError = Validaciones.telefono(state.telefono)
        val passwordError = Validaciones.password(state.password)
        val confirmarError = Validaciones.confirmar(state.password, state.confirmar)
        if (nombreError != null || emailError != null || telefonoError != null ||
            passwordError != null || confirmarError != null
        ) {
            _uiState.update {
                it.copy(
                    nombreError = nombreError,
                    emailError = emailError,
                    telefonoError = telefonoError,
                    passwordError = passwordError,
                    confirmarError = confirmarError
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                withContext(Dispatchers.IO) {
                    usuarioDao.insertUsuario(
                        Usuario(
                            nombre = state.nombre.trim(),
                            email = state.email.trim(),
                            telefono = state.telefono.trim(),
                            passwordHash = state.password,
                            rol = ROL_TECNICO,
                            estado = ESTADO_PENDIENTE
                        )
                    )
                }
                _uiState.update { it.copy(isLoading = false, success = true) }
            } catch (e: SQLiteConstraintException) {
                _uiState.update {
                    it.copy(isLoading = false, error = "El correo ya está registrado")
                }
            }
        }
    }

    fun reset() {
        _uiState.value = RegistroUiState()
    }
}
