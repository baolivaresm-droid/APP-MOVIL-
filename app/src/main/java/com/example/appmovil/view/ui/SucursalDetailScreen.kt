package com.example.appmovil.view.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appmovil.repository.models.Maquina
import com.example.appmovil.viewmodels.Validaciones
import com.example.appmovil.repository.models.ESTADO_APROBADO
import com.example.appmovil.repository.models.ESTADO_PENDIENTE
import com.example.appmovil.view.utils.categoriasMaquina
import com.example.appmovil.view.utils.imagenDeCategoria
import com.example.appmovil.viewmodels.EmpresaViewModel
import com.example.appmovil.viewmodels.MaquinaViewModel
import com.example.appmovil.viewmodels.SucursalViewModel

// Detalle / historial de la sucursal (CasoA pantalla 4: catálogo por tipo de equipo).
// - Agrupa por categoría con foto de referencia compartida.
// - Empty State dinámico: solo muestra categorías con equipos.
// - El técnico puede proponer maquinaria nueva; queda pendiente de validación.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SucursalDetailScreen(
    sucursalId: Long,
    usuarioId: Long,
    sucursalViewModel: SucursalViewModel,
    empresaViewModel: EmpresaViewModel,
    maquinaViewModel: MaquinaViewModel,
    onNavigateToMedicion: (Long) -> Unit
) {
    // La sucursal y su empresa se resuelven desde Room (solo viaja el id por la ruta)
    val sucursales by sucursalViewModel.sucursales.collectAsStateWithLifecycle()
    val sucursal = sucursales.find { it.id == sucursalId }
    val sucursalNombre = sucursal?.nombre.orEmpty()
    val direccion = sucursal?.direccion.orEmpty()
    val comuna = sucursal?.comuna.orEmpty()
    val telefono = sucursal?.telefono.orEmpty()
    var empresaNombre by remember { mutableStateOf("") }
    LaunchedEffect(sucursal?.empresaId) {
        empresaNombre = sucursal?.let { empresaViewModel.getEmpresa(it.empresaId)?.nombre }.orEmpty()
    }
    LaunchedEffect(sucursalId) { maquinaViewModel.observarSucursal(sucursalId) }
    val maquinas by maquinaViewModel.maquinas.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var categoriaPreseleccionada by remember { mutableStateOf<String?>(null) }

    val porCategoria = remember(maquinas) { maquinas.groupBy { it.categoria }.toSortedMap() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)) {
            // Encabezado con la sucursal y la empresa del cliente
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = sucursalNombre.ifBlank { "Detalle de planta" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (empresaNombre.isNotBlank()) "$empresaNombre · $comuna" else comuna,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Cabecera con los datos de la ubicación: el técnico necesita
            // llegar al sitio y saber a quién reportar.
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = direccion,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (telefono.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.height(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = telefono,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (maquinas.isEmpty()) {
                // Estado Vacío: aún no hay equipos en esta sucursal.
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Sin maquinaria registrada",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Si en esta sucursal hay equipos que aún no están registrados, agrégalos con el botón de abajo: el administrador los validará.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    porCategoria.forEach { (categoria, lista) ->
                        item(key = "cat_$categoria") {
                            CategoriaCard(
                                categoria = categoria,
                                maquinas = lista,
                                onAddCodigo = {
                                    categoriaPreseleccionada = categoria
                                    showAddDialog = true
                                },
                                onMaquinaClick = onNavigateToMedicion
                            )
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                categoriaPreseleccionada = null
                showAddDialog = true
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Agregar maquinaria") }
        )
    }

    if (showAddDialog) {
        AgregarMaquinaDialog(
            categoriaInicial = categoriaPreseleccionada ?: categoriasMaquina.first().nombre,
            codigosExistentes = maquinas.map { it.codigo },
            onDismiss = { showAddDialog = false },
            onConfirm = { categoria, codigo ->
                // La maquinaria propuesta por el técnico queda pendiente de validación.
                maquinaViewModel.guardarMaquina(
                    sucursalId = sucursalId,
                    categoria = categoria,
                    codigo = codigo,
                    creadoPor = usuarioId,
                    estado = ESTADO_PENDIENTE
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CategoriaCard(
    categoria: String,
    maquinas: List<Maquina>,
    onAddCodigo: () -> Unit,
    onMaquinaClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Foto de referencia del equipo: la imagen de su categoría.
            Surface(
                modifier = Modifier.fillMaxWidth().height(110.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Image(
                    painter = painterResource(id = imagenDeCategoria(categoria)),
                    contentDescription = categoria,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoria,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onAddCodigo) {
                    Text("+ Agregar código")
                }
            }
            // Sub-lista filtrada con los códigos específicos de la categoría.
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                maquinas.forEach { maquina ->
                    CodigoRow(
                        maquina = maquina,
                        onClick = { onMaquinaClick(maquina.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CodigoRow(
    maquina: Maquina,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = maquina.codigo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            if (maquina.estado != ESTADO_APROBADO) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Pendiente",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = "Ver historial",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgregarMaquinaDialog(
    categoriaInicial: String,
    codigosExistentes: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var categoria by remember(categoriaInicial) { mutableStateOf(categoriaInicial) }
    var codigo by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val codigoError = Validaciones.codigoMaquina(codigo).takeIf { codigo.isNotBlank() }
        ?: if (codigosExistentes.any { it.equals(codigo.trim(), ignoreCase = true) }) {
            "Ese código ya existe en esta sucursal"
        } else null
    val esValido = Validaciones.codigoMaquina(codigo) == null &&
        codigosExistentes.none { it.equals(codigo.trim(), ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva maquinaria") },
        text = {
            Column {
                Box {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría *") },
                        trailingIcon = {
                            TextButton(onClick = { expanded = !expanded }) {
                                Text(if (expanded) "▲" else "▼")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categoriasMaquina.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.nombre) },
                                onClick = {
                                    categoria = cat.nombre
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it.uppercase().filter { c -> c.isLetterOrDigit() || c == '-' } },
                    label = { Text("Código del equipo *") },
                    placeholder = { Text("Ej: REL-123") },
                    singleLine = true,
                    isError = codigoError != null,
                    supportingText = { codigoError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "La foto de referencia se pedirá en la primera medición de esta categoría y se reutilizará en las siguientes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(categoria, codigo.trim()) }, enabled = esValido) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
