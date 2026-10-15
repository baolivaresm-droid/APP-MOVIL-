package com.example.appmovil.ui.screen

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.appmovil.data.local.maquina.MaquinaEntity
import com.example.appmovil.domain.validation.validateCodigoMaquina
import com.example.appmovil.model.categoriasMaquina
import com.example.appmovil.model.imagenDeCategoria
import com.example.appmovil.viewmodel.MaquinaViewModel

// Detalle / historial de la empresa (CasoA pantalla 4: catálogo por tipo de equipo).
// - Agrupa por categoría con foto de referencia compartida.
// - Empty State dinámico: solo muestra categorías con equipos.
// - Botón agregar maquinaria + sub-lista de códigos + botón agregar otro código.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpresaDetailScreen(
    empresaId: Long,
    empresaNombre: String,
    empresaZona: String,
    maquinaViewModel: MaquinaViewModel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onNavigateToMedicion: (Long) -> Unit
) {
    LaunchedEffect(empresaId) { maquinaViewModel.observarEmpresa(empresaId) }
    val maquinas by maquinaViewModel.maquinas.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var categoriaPreseleccionada by remember { mutableStateOf<String?>(null) }

    val porCategoria = remember(maquinas) { maquinas.groupBy { it.categoria }.toSortedMap() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(empresaNombre.ifBlank { "Detalle de planta" })
                        if (empresaZona.isNotBlank()) {
                            Text(
                                text = empresaZona,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onHomeClick) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Ir al Home")
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
                onClick = {
                    categoriaPreseleccionada = null
                    showAddDialog = true
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Agregar maquinaria") }
            )
        }
    ) { innerPadding ->
        if (maquinas.isEmpty()) {
            // Estado Vacío: aún no hay equipos en esta planta.
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
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
                    text = "Agrega la primera maquinaria de esta planta para comenzar el historial.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(12.dp),
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
                            onMaquinaClick = onNavigateToMedicion,
                            onDelete = { maquinaViewModel.eliminarMaquina(it) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AgregarMaquinaDialog(
            categoriaInicial = categoriaPreseleccionada ?: categoriasMaquina.first().nombre,
            codigosExistentes = maquinas.map { it.codigo },
            onDismiss = { showAddDialog = false },
            onConfirm = { categoria, codigo ->
                maquinaViewModel.guardarMaquina(
                    empresaId = empresaId,
                    categoria = categoria,
                    codigo = codigo,
                    fotoRefUri = ""
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CategoriaCard(
    categoria: String,
    maquinas: List<MaquinaEntity>,
    onAddCodigo: () -> Unit,
    onMaquinaClick: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    val fotoRef = maquinas.firstOrNull { it.fotoRefUri.isNotBlank() }?.fotoRefUri.orEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(110.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                if (fotoRef.isNotBlank()) {
                    AsyncImage(
                        model = fotoRef,
                        contentDescription = "Foto referencia $categoria",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = imagenDeCategoria(categoria)),
                        contentDescription = categoria,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
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
                        onClick = { onMaquinaClick(maquina.id) },
                        onDelete = { onDelete(maquina.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CodigoRow(
    maquina: MaquinaEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
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
            Text(
                text = "Ver historial",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onDelete) {
                Text("Quitar", color = MaterialTheme.colorScheme.error)
            }
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

    val codigoError = validateCodigoMaquina(codigo).takeIf { codigo.isNotBlank() }
        ?: if (codigosExistentes.any { it.equals(codigo.trim(), ignoreCase = true) }) {
            "Ese código ya existe en esta empresa"
        } else null
    val esValido = validateCodigoMaquina(codigo) == null &&
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
