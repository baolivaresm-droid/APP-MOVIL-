package com.example.appmovil.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appmovil.data.local.empresa.EmpresaEntity
import com.example.appmovil.data.local.storage.UserPreferences
import com.example.appmovil.domain.validation.validateEmpresaNombre
import com.example.appmovil.domain.validation.validateZona
import kotlinx.coroutines.delay

// Home CasoA pantalla 3: lista de empresas / ubicaciones en terreno.
// Inicia con Estado Vacío si no hay registros + diálogo para agregar empresa (nombre + zona).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    empresas: List<EmpresaEntity>,
    onAddEmpresa: (String, String) -> Unit,
    onDeleteEmpresa: (Long) -> Unit,
    onNavigateToEmpresa: (Long) -> Unit,
    onNavigateToProfile: () -> Unit,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean = false
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showThemeMsg by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var empresaAEliminar by remember { mutableStateOf<EmpresaEntity?>(null) }

    // Ej.5: el mensaje de confirmación se auto-oculta a los 2 segundos.
    LaunchedEffect(showThemeMsg) {
        if (showThemeMsg) {
            delay(2000)
            showThemeMsg = false
        }
    }

    // Ej.8: estado de sesión desde DataStore para el icono Person/PersonOff
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val isLoggedIn by userPrefs.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)

    // Ej.5: animar color según tema
    val themeColor by animateColorAsState(
        targetValue = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        animationSpec = tween(durationMillis = 500),
        label = "themeColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Empresas en Terreno - Induztek") },
                navigationIcon = {
                    Icon(
                        imageVector = if (isLoggedIn) Icons.Filled.Person else Icons.Filled.PersonOff,
                        contentDescription = if (isLoggedIn) "Sesión activa" else "Sesión inactiva",
                        tint = themeColor,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menú")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Mi Perfil (Técnico)") },
                            onClick = { menuExpanded = false; onNavigateToProfile() }
                        )
                        DropdownMenuItem(
                            text = { Text("Cambiar Tema (Claro/Oscuro)") },
                            onClick = {
                                menuExpanded = false
                                onToggleTheme()
                                showThemeMsg = true
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error) },
                            onClick = { menuExpanded = false; onLogout() }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Agregar empresa") }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Ej.5 AnimatedVisibility: retroalimentación al cambiar tema
            AnimatedVisibility(visible = showThemeMsg) {
                Text(
                    text = if (isDarkTheme) "Modo oscuro activado" else "Modo claro activado",
                    style = MaterialTheme.typography.labelLarge,
                    color = themeColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            if (empresas.isEmpty()) {
                // Estado Vacío (CasoA 3): guía al técnico a crear la primera empresa.
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = "Sin empresas registradas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Agrega tu primera empresa o ubicación en terreno para comenzar las inspecciones.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(empresas, key = { it.id }) { empresa ->
                        EmpresaCard(
                            empresa = empresa,
                            onClick = { onNavigateToEmpresa(empresa.id) },
                            onDelete = { empresaAEliminar = empresa }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AgregarEmpresaDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { nombre, zona ->
                onAddEmpresa(nombre, zona)
                showAddDialog = false
            }
        )
    }

    empresaAEliminar?.let { empresa ->
        AlertDialog(
            onDismissRequest = { empresaAEliminar = null },
            title = { Text("Eliminar empresa") },
            text = { Text("Se eliminará \"${empresa.nombre}\" y toda su maquinaria y mediciones. ¿Continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteEmpresa(empresa.id)
                    empresaAEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { empresaAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun EmpresaCard(
    empresa: EmpresaEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = empresa.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = empresa.zona,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar empresa",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AgregarEmpresaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var zona by remember { mutableStateOf("") }

    val nombreError = validateEmpresaNombre(nombre).takeIf { nombre.isNotBlank() }
    val zonaError = validateZona(zona).takeIf { zona.isNotBlank() }
    val esValido = validateEmpresaNombre(nombre) == null && validateZona(zona) == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva empresa en terreno") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre empresa *") },
                    placeholder = { Text("Ej: Planta Maipú") },
                    singleLine = true,
                    isError = nombreError != null,
                    supportingText = { nombreError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = zona,
                    onValueChange = { zona = it },
                    label = { Text("Zona en terreno *") },
                    placeholder = { Text("Ej: Subestación norte") },
                    singleLine = true,
                    isError = zonaError != null,
                    supportingText = { zonaError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(nombre.trim(), zona.trim()) }, enabled = esValido) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
