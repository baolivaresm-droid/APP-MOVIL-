package com.example.appmovil.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.appmovil.models.UserEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: UserEntity,
    onUpdateProfile: (String, String) -> Unit, // (nuevaBase, nuevaFotoUri)
    onBackClick: () -> Unit
) {
    var direccion by remember { mutableStateOf(user.direccion) }
    var fotoUri by remember { mutableStateOf(user.fotoUri) }

    // Lanzador para seleccionar imagen desde la Galería (Recurso nativo requerido)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fotoUri = it.toString()
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar de Perfil
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para abrir la galería y actualizar la foto de perfil nativa
            OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                Text("Actualizar Fotografía (Galería)")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campos informativos (Solo lectura)
            OutlinedTextField(
                value = user.nombre,
                onValueChange = {},
                label = { Text("Nombre del Técnico") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = user.email,
                onValueChange = {},
                label = { Text("Correo Institucional") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Zona / Base Asignada (Adaptado del campo dirección)
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Zona / Base de Operaciones Asignada") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botón Guardar Cambios
            Button(
                onClick = { onUpdateProfile(direccion, fotoUri) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Guardar Cambios de Perfil", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}