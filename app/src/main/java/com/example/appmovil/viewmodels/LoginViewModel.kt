package com.example.appmovil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovil.repository.data.AppDatabase
import com.example.appmovil.repository.models.ESTADO_APROBADO
import com.example.appmovil.repository.models.ROL_TECNICO
import com.example.appmovil.repository.models.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val rol: String = ROL_TECNICO,
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedUser: Usuario? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioDao = AppDatabase.getDatabase(application).usuarioDao()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value, emailError = Validaciones.email(value), error = null)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = null, error = null)
    }

    // El acceso elegido en pantalla: técnico (por defecto) o administrador
    fun onRolChange(rol: String) = _uiState.update { it.copy(rol = rol, error = null) }

    fun login() {
        val state = _uiState.value
        val emailError = Validaciones.email(state.email)
        val passwordError = if (state.password.isBlank()) "La contraseña es obligatoria" else null
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val usuario = withContext(Dispatchers.IO) {
                usuarioDao.login(state.email.trim(), state.password)
            }
            _uiState.update {
                when {
                    usuario == null ->
                        it.copy(isLoading = false, error = "Credenciales inválidas")
                    usuario.rol != state.rol ->
                        it.copy(
                            isLoading = false,
                            error = "Ese usuario no tiene acceso de ${state.rol.lowercase()}"
                        )
                    usuario.estado != ESTADO_APROBADO ->
                        it.copy(
                            isLoading = false,
                            error = "Tu cuenta está pendiente de aprobación del administrador"
                        )
                    else -> it.copy(
                        isLoading = false,
                        error = null,
                        password = "",
                        loggedUser = usuario
                    )
                }
            }
        }
    }

    // Perfil: el técnico actualiza su zona/base y su foto
    fun actualizarPerfil(direccion: String, fotoUri: String) {
        val usuario = _uiState.value.loggedUser ?: return
        val actualizado = usuario.copy(direccion = direccion, fotoUri = fotoUri)
        viewModelScope.launch {
            withContext(Dispatchers.IO) { usuarioDao.updateUsuario(actualizado) }
            _uiState.update { it.copy(loggedUser = actualizado) }
        }
    }

    fun logout() {
        _uiState.value = LoginUiState()
    }
}
