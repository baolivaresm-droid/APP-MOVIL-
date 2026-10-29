package com.example.appmovil.view.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appmovil.view.utils.AnimacionEntradaFormulario
import com.example.appmovil.viewmodels.RegistroViewModel

// Registro de técnico: 5 campos con validación por campo y foto pendiente de aprobación.
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel,
    onRegisteredNavigateLogin: () -> Unit,
    onGoLogin: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirm by rememberSaveable { mutableStateOf(false) }
    // ANIMACIÓN 1 (view/utils/Animations.kt): entrada del formulario
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Mensaje de éxito: confirma el registro y luego limpia el formulario y va al Login
    if (state.success) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("¡Registro exitoso!") },
            text = {
                Text(
                    "Tu cuenta de técnico fue creada y quedó pendiente de aprobación " +
                        "del administrador de Induztek. Te avisaremos cuando puedas iniciar sesión."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.reset()
                        onRegisteredNavigateLogin()
                    }
                ) {
                    Text("Ir al Login")
                }
            }
        )
    }

    // ANIMACIÓN 1 (view/utils/Animations.kt): entrada del formulario
    AnimacionEntradaFormulario(visible = visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(text = "Registro de Técnico", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(text = "Induztek Ingeniería SpA", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.nombre,
                    onValueChange = viewModel::onNombreChange,
                    label = { Text("Nombre y apellido del técnico") },
                    singleLine = true,
                    isError = state.nombreError != null,
                    supportingText = {
                        state.nombreError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = "Nombre") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("Correo institucional") },
                    singleLine = true,
                    isError = state.emailError != null,
                    supportingText = {
                        state.emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.telefono,
                    onValueChange = viewModel::onTelefonoChange,
                    label = { Text("Teléfono (opcional)") },
                    singleLine = true,
                    isError = state.telefonoError != null,
                    supportingText = {
                        state.telefonoError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Teléfono") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("Contraseña segura") },
                    singleLine = true,
                    isError = state.passwordError != null,
                    supportingText = {
                        state.passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    visualTransformation =
                        if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector =
                                    if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPassword) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.confirmar,
                    onValueChange = viewModel::onConfirmarChange,
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    isError = state.confirmarError != null,
                    supportingText = {
                        state.confirmarError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    visualTransformation =
                        if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Icon(
                                imageVector =
                                    if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showConfirm) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = viewModel::registrar,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Creando cuenta...")
                    } else {
                        Text("Registrar Cuenta")
                    }
                }

                state.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onGoLogin, modifier = Modifier.fillMaxWidth()) {
                    Text("¿Ya estás registrado? Inicia sesión")
                }
            }
        }
    }
}
