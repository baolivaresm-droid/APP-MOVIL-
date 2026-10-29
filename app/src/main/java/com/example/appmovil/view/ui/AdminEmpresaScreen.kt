package com.example.appmovil.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appmovil.repository.models.Sucursal
import com.example.appmovil.repository.models.Usuario
import com.example.appmovil.repository.models.ESTADO_APROBADO
import com.example.appmovil.viewmodels.AdminViewModel
import com.example.appmovil.viewmodels.Validaciones

// Sucursales de una empresa (CasoA: panel del administrador).
// Acá el administrador ve las sucursales del cliente y decide a qué técnicos
// se le asignan: lo no asignado es invisible para los técnicos.
@Composable
fun AdminEmpresaScreen(
    empresaId: Long,
    adminViewModel: AdminViewModel,
    onAddSucursal: (Long, String, String, String, String) -> Unit
) {
    val empresas by adminViewModel.empresas.collectAsStateWithLifecycle()
    val tecnicos by adminViewModel.tecnicos.collectAsStateWithLifecycle()
    val todasLasSucursales by adminViewModel.sucursales.collectAsStateWithLifecycle()
    val empresa = empresas.find { it.id == empresaId }
    val sucursales = todasLasSucursales.filter { it.empresaId == empresaId }

    var showAddSucursal by remember { mutableStateOf(false) }
    var showEditSucursal by remember { mutableStateOf(false) }
    var sucursalParaAsignar by remember { mutableStateOf<Sucursal?>(null) }
    var mostrarTecnicos by remember { mutableStateOf(false) }
    var aEliminar by remember { mutableStateOf<Sucursal?>(null) }
    var sucursalEditando by remember { mutableStateOf<Sucursal?>(null) }

    if (empresa == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cargando empresa...")
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)) {
            // Encabezado del cliente
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = empresa.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "RUT ${empresa.rut}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Asignar la empresa completa a un técnico: es la forma rápida de
            // darle todo el trabajo de ese cliente.
            if (sucursales.isNotEmpty() && tecnicos.isNotEmpty()) {
                OutlinedButton(
                    onClick = { mostrarTecnicos = true },
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Asignar ${empresa.nombre} completa a un técnico")
                }
            }
            if (sucursales.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Sin sucursales registradas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Agrega la sucursal para indicar al técnico la ubicación exacta donde debe trabajar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                items(sucursales, key = { it.id }) { sucursal ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sucursal.nombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${sucursal.direccion}, ${sucursal.comuna}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (sucursal.telefono.isNotBlank()) {
                                        Text(
                                            text = sucursal.telefono,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(onClick = { 
                                    sucursalEditando = sucursal 
                                    showEditSucursal = true 
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Editar sucursal",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { aEliminar = sucursal }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Eliminar sucursal",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { sucursalParaAsignar = sucursal },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Group,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Asignar a técnicos")
                            }
                        }
                    }
                }
            }
        }
        }

        ExtendedFloatingActionButton(
            onClick = { showAddSucursal = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Agregar sucursal") }
        )
    }

    if (mostrarTecnicos) {
        AlertDialog(
            onDismissRequest = { mostrarTecnicos = false },
            title = { Text("Asignar ${empresa.nombre} completa") },
            text = {
                Column {
                    Text(
                        text = "El técnico podrá ver esta empresa con todas sus sucursales.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    tecnicos.forEach { tecnico ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tecnico.nombre,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = tecnico.email,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = {
                                adminViewModel.asignarEmpresaCompleta(tecnico.id, empresa.id)
                                mostrarTecnicos = false
                            }) { Text("Asignar") }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarTecnicos = false }) { Text("Cerrar") }
            }
        )
    }

    if (showAddSucursal) {
        AgregarSucursalAdminDialog(
            onDismiss = { showAddSucursal = false },
            onConfirm = { nombre, direccion, comuna, telefono ->
                onAddSucursal(empresa.id, nombre, direccion, comuna, telefono)
                showAddSucursal = false
            }
        )
    }

    if (showEditSucursal) {
        EditarSucursalAdminDialog(
            sucursal = sucursalEditando,
            onDismiss = { showEditSucursal = false },
            onConfirm = { nombre, direccion, comuna, telefono ->
                sucursalEditando?.let { adminViewModel.editarSucursal(it.id, nombre, direccion, comuna, telefono) }
                showEditSucursal = false
            }
        )
    }

    sucursalParaAsignar?.let { sucursal ->
        AsignarTecnicosDialog(
            sucursal = sucursal,
            tecnicos = tecnicos,
            onDismiss = { sucursalParaAsignar = null },
            onAsignar = { usuarioId ->
                adminViewModel.asignarSucursal(usuarioId, sucursal.id)
            },
            onDesasignar = { usuarioId ->
                adminViewModel.desasignarSucursal(usuarioId, sucursal.id)
            }
        )
    }

    aEliminar?.let { sucursal ->
        AlertDialog(
            onDismissRequest = { aEliminar = null },
            title = { Text("Eliminar sucursal") },
            text = {
                Text("Se eliminará \"${sucursal.nombre}\" y toda su maquinaria e informes. ¿Continuar?")
            },
            confirmButton = {
                TextButton(onClick = {
                    adminViewModel.eliminarSucursal(sucursal.id)
                    aEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { aEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

// Asignación de una sucursal a uno o varios técnicos.
@Composable
private fun AsignarTecnicosDialog(
    sucursal: Sucursal,
    tecnicos: List<Usuario>,
    onDismiss: () -> Unit,
    onAsignar: (Long) -> Unit,
    onDesasignar: (Long) -> Unit
) {
    // selected: ids de técnicos marcados en el dialog.
    val seleccionados = remember { mutableStateOf(setOf<Long>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar ${sucursal.nombre}") },
        text = {
            Column {
                Text(
                    text = "Marca los técnicos que pueden trabajar en esta sucursal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (tecnicos.isEmpty()) {
                    Text(
                        text = "No hay técnicos registrados.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    tecnicos.forEach { tecnico ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = tecnico.id in seleccionados.value,
                                onCheckedChange = { marcado ->
                                    seleccionados.value = if (marcado) {
                                        seleccionados.value + tecnico.id
                                    } else {
                                        seleccionados.value - tecnico.id
                                    }
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tecnico.nombre,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = if (tecnico.estado == ESTADO_APROBADO) {
                                        "Aprobado · ${tecnico.email}"
                                    } else {
                                        "Pendiente de aprobación · ${tecnico.email}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onAsignar(tecnico.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Asignar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                seleccionados.value.forEach { onAsignar(it) }
                onDismiss()
            }) { Text("Asignar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
private fun AgregarSucursalAdminDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var comuna by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    val nombreError = Validaciones.sucursalNombre(nombre).takeIf { nombre.isNotBlank() }
    val direccionError = Validaciones.sucursalDireccion(direccion).takeIf { direccion.isNotBlank() }
    val comunaError = Validaciones.zona(comuna).takeIf { comuna.isNotBlank() }
    val esValido = Validaciones.sucursalNombre(nombre) == null &&
        Validaciones.sucursalDireccion(direccion) == null &&
        Validaciones.zona(comuna) == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva sucursal") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre sucursal *") },
                    placeholder = { Text("Ej: Jumbo La Florida") },
                    singleLine = true,
                    isError = nombreError != null,
                    supportingText = { nombreError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección exacta *") },
                    placeholder = { Text("Ej: Av. José Pedro Alt 800") },
                    singleLine = true,
                    isError = direccionError != null,
                    supportingText = { direccionError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comuna,
                    onValueChange = { comuna = it },
                    label = { Text("Comuna *") },
                    placeholder = { Text("Ej: La Florida") },
                    singleLine = true,
                    isError = comunaError != null,
                    supportingText = { comunaError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it.filter { c -> c.isDigit() || c == '+' } },
                    label = { Text("Teléfono de contacto (opcional)") },
                    placeholder = { Text("Ej: +56920000001") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(nombre.trim(), direccion.trim(), comuna.trim(), telefono.trim()) },
                enabled = esValido
            ) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun EditarSucursalAdminDialog(
    sucursal: Sucursal?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf(sucursal?.nombre.orEmpty()) }
    var direccion by remember { mutableStateOf(sucursal?.direccion.orEmpty()) }
    var comuna by remember { mutableStateOf(sucursal?.comuna.orEmpty()) }
    var telefono by remember { mutableStateOf(sucursal?.telefono.orEmpty()) }

    val nombreError = Validaciones.sucursalNombre(nombre).takeIf { nombre.isNotBlank() }
    val direccionError = Validaciones.sucursalDireccion(direccion).takeIf { direccion.isNotBlank() }
    val comunaError = Validaciones.zona(comuna).takeIf { comuna.isNotBlank() }
    val esValido = Validaciones.sucursalNombre(nombre) == null &&
        Validaciones.sucursalDireccion(direccion) == null &&
        Validaciones.zona(comuna) == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar sucursal") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre sucursal *") },
                    placeholder = { Text("Ej: Jumbo La Florida") },
                    singleLine = true,
                    isError = nombreError != null,
                    supportingText = { nombreError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección exacta *") },
                    placeholder = { Text("Ej: Av. José Pedro Alt 800") },
                    singleLine = true,
                    isError = direccionError != null,
                    supportingText = { direccionError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comuna,
                    onValueChange = { comuna = it },
                    label = { Text("Comuna *") },
                    placeholder = { Text("Ej: La Florida") },
                    singleLine = true,
                    isError = comunaError != null,
                    supportingText = { comunaError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it.filter { c -> c.isDigit() || c == '+' } },
                    label = { Text("Teléfono de contacto (opcional)") },
                    placeholder = { Text("Ej: +56920000001") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(nombre.trim(), direccion.trim(), comuna.trim(), telefono.trim()) },
                enabled = esValido
            ) { Text("Guardar cambios") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
