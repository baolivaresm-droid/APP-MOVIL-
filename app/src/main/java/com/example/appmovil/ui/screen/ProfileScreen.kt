package com.example.appmovil.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appmovil.data.local.user.UserEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Patrón Ej.8 HomeScreen cámara: archivo temporal + FileProvider + TakePicture + Coil.
private fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
    // Crear físicamente el archivo: si no existe, algunas apps de cámara fallan al escribir.
    val file = File(storageDir, "IMG_$timeStamp.jpg")
    if (!file.exists()) file.createNewFile()
    return file
}

private fun getImageUriForFile(context: Context, file: File): Uri {
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: UserEntity,
    onUpdateProfile: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var direccion by remember { mutableStateOf(user.direccion) }
    var fotoUri by rememberSaveable { mutableStateOf(user.fotoUri) }
    // Guardar como String con rememberSaveable: si el sistema destruye la Activity
    // mientras la cámara está abierta, el Uri no se pierde al volver.
    var pendingCaptureUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Recurso nativo 1: Cámara (Ej.6 CameraX simplificado a TakePicture como Ej.8)
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCaptureUriString?.let { fotoUri = it }
            Toast.makeText(context, "Foto tomada correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "No se tomó ninguna foto", Toast.LENGTH_SHORT).show()
        }
        pendingCaptureUriString = null
    }

    // Lanza la cámara creando el archivo temporal de forma segura (evita el crash).
    fun launchCamera() {
        try {
            val file = createTempImageFile(context)
            val uri = getImageUriForFile(context, file)
            pendingCaptureUriString = uri.toString()
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            pendingCaptureUriString = null
            Toast.makeText(context, "No se pudo abrir la cámara: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Permiso runtime CAMERA: TakePicture NO lo pide solo, sin esto crashea con
    // SecurityException en muchos equipos/emuladores.
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_LONG).show()
        }
    }

    fun onTakePhotoClick() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Recurso nativo 2: Galería moderna PhotoPicker (Ej.6 storage PhotoPicker)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Persistir permiso de lectura para que la foto sobreviva reinicios.
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { /* PhotoPicker ya otorga acceso temporal */ }
            fotoUri = it.toString()
            Toast.makeText(context, "Foto de galería seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Técnico en Terreno") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(120.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (fotoUri.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(Uri.parse(fotoUri)).crossfade(true).build(),
                        contentDescription = "Foto del técnico",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onTakePhotoClick() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tomar Fotografía (Cámara)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Actualizar Fotografía (Galería)")
            }
            if (fotoUri.isNotBlank()) {
                TextButton(onClick = { showDeleteDialog = true }) {
                    Text("Eliminar foto", color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = user.nombre,
                onValueChange = {},
                label = { Text("Nombre del Técnico") },
                leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = user.email,
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
                label = { Text("Zona / Base de Operaciones Asignada") },
                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                supportingText = {
                    if (direccion.isBlank()) Text("Indica tu base, ej: Plaza Norte", color = MaterialTheme.colorScheme.error)
                },
                isError = direccion.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { onUpdateProfile(direccion, fotoUri) },
                enabled = direccion.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(text = "Guardar Cambios de Perfil", style = MaterialTheme.typography.bodyLarge)
            }
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
