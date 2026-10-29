package com.example.appmovil.view.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmovil.repository.models.ROL_ADMIN
import com.example.appmovil.repository.models.Usuario
import com.example.appmovil.view.utils.CameraCapture
import com.example.appmovil.view.utils.PhotoBox

// Perfil del usuario: foto (cámara CameraX o galería) y zona / base de operaciones.
@Composable
fun ProfileScreen(
    usuario: Usuario?,
    onUpdateProfile: (String, String) -> Unit
) {
    val context = LocalContext.current
    var direccion by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.direccion.orEmpty()) }
    var fotoUri by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.fotoUri.orEmpty()) }
    var showCamera by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // ANIMACIÓN 1: cámara a pantalla completa (view/utils/Animations.kt)
    if (showCamera) {
        CameraCapture(
            carpeta = "perfiles",
            onImageCaptured = { uri ->
                fotoUri = uri.toString()
                showCamera = false
                Toast.makeText(context, "Foto tomada correctamente", Toast.LENGTH_SHORT).show()
            },
            onClose = { showCamera = false }
        )
        return
    }

    // Galería moderna (PhotoPicker) como alternativa a la cámara
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { /* PhotoPicker ya otorga acceso temporal */ }
            fotoUri = it.toString()
            Toast.makeText(context, "Foto de galería seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mi Perfil",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = if (usuario?.rol == ROL_ADMIN) "Administrador · Induztek Ingeniería SpA" else "Técnico en terreno · Induztek Ingeniería SpA",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Componente reutilizable de foto (view/utils/PhotoBox.kt)
        PhotoBox(
            imageUri = fotoUri.ifBlank { null },
            circle = true,
            onTakePhoto = { showCamera = true }
        )

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { showCamera = true },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Tomar Fotografía (Cámara)") }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Actualizar Fotografía (Galería)") }
        if (fotoUri.isNotBlank()) {
            TextButton(onClick = { showDeleteDialog = true }) {
                Text("Eliminar foto", color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = usuario?.nombre.orEmpty(),
            onValueChange = {},
            label = { Text("Nombre del Técnico") },
            leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = usuario?.email.orEmpty(),
            onValueChange = {},
            label = { Text("Correo Institucional") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            label = { Text("Zona / Base de Operaciones") },
            leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
            supportingText = {
                if (direccion.isBlank()) {
                    Text("Indica tu base, ej: Plaza Norte", color = MaterialTheme.colorScheme.error)
                }
            },
            isError = direccion.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onUpdateProfile(direccion, fotoUri) },
            enabled = direccion.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(text = "Guardar Cambios de Perfil", style = MaterialTheme.typography.bodyLarge)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmación") },
            text = { Text("¿Desea eliminar la foto?") },
            confirmButton = {
                TextButton(onClick = {
                    fotoUri = ""
                    showDeleteDialog = false
                    Toast.makeText(context, "Foto eliminada", Toast.LENGTH_LONG).show()
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
