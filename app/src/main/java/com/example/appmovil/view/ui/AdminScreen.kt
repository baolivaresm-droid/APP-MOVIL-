package com.example.appmovil.view.ui

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appmovil.repository.models.Asignacion
import com.example.appmovil.repository.models.Empresa
import com.example.appmovil.repository.models.Informe
import com.example.appmovil.repository.models.Maquina
import com.example.appmovil.repository.models.Sucursal
import com.example.appmovil.repository.models.Usuario
import com.example.appmovil.repository.models.ESTADO_APROBADO
import com.example.appmovil.repository.models.ESTADO_PENDIENTE
import com.example.appmovil.repository.models.ESTADO_RECHAZADO
import com.example.appmovil.view.utils.categoriasMaquina
import com.example.appmovil.viewmodels.AdminViewModel
import com.example.appmovil.viewmodels.Validaciones
import com.example.appmovil.viewmodels.InformeViewModel
import java.io.File

// Panel de administración de Induztek.
// Cuatro secciones: cuentas de técnicos, empresas, maquinaria pendiente de
// validar e historial de protocolos emitidos.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    adminViewModel: AdminViewModel,
    informeViewModel: InformeViewModel,
    onNavigateToEmpresa: (Long) -> Unit
) {
    val context = LocalContext.current
    val tecnicos by adminViewModel.tecnicos.collectAsStateWithLifecycle()
    val empresas by adminViewModel.empresas.collectAsStateWithLifecycle()
    val sucursales by adminViewModel.sucursales.collectAsStateWithLifecycle()
    val maquinasPendientes by adminViewModel.maquinasPendientes.collectAsStateWithLifecycle()
    val asignaciones by adminViewModel.asignaciones.collectAsStateWithLifecycle()
    val informes by informeViewModel.informes.collectAsStateWithLifecycle()

    var tab by remember { mutableStateOf(0) }

    var showAddEmpresa by remember { mutableStateOf(false) }
    var showAddMaquina by remember { mutableStateOf(false) }
    var showEditEmpresa by remember { mutableStateOf(false) }
    var empresaEditando by remember { mutableStateOf<Empresa?>(null) }
    var showEditMaquina by remember { mutableStateOf(false) }
    var maquinaEditando by remember { mutableStateOf<Maquina?>(null) }
    var aEliminar by remember { mutableStateOf<Pair<String, Long>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)) {
            PrimaryScrollableTabRow(
                selectedTabIndex = tab,
                edgePadding = 8.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = { Text("Técnicos (${tecnicos.count { it.estado == ESTADO_PENDIENTE }})") }
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text("Empresas") }
                )
                Tab(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    text = { Text("Maquinaria (${maquinasPendientes.size})") }
                )
                Tab(
                    selected = tab == 3,
                    onClick = { tab = 3 },
                    text = { Text("Informes (${informes.size})") }
                )
            }

            when (tab) {
                0 -> ListaTecnicos(
                    tecnicos = tecnicos,
                    asignaciones = asignaciones,
                    onAprobar = { adminViewModel.aprobar(it) },
                    onRechazar = { adminViewModel.rechazar(it) }
                )
                1 -> ListaEmpresas(
                    empresas = empresas,
                    onNavigateToEmpresa = onNavigateToEmpresa,
                    onEditar = { id ->
                        empresas.find { it.id == id }?.let { 
                            empresaEditando = it 
                            showEditEmpresa = true 
                        }
                    },
                    onEliminar = { aEliminar = "Empresa" to it }
                )
                2 -> ListaMaquinas(
                    maquinas = maquinasPendientes,
                    sucursales = sucursales,
                    onAprobar = { adminViewModel.aprobarMaquina(it) },
                    onRechazar = { adminViewModel.rechazarMaquina(it) },
                    onEditar = { id ->
                        maquinasPendientes.find { it.id == id }?.let { 
                            maquinaEditando = it 
                            showEditMaquina = true 
                        }
                    }
                )
                else -> ListaInformes(
                    informes = informes,
                    empresas = empresas,
                    sucursales = sucursales,
                    onCompartir = { informe ->
                        val archivo = File(informe.pdfPath)
                        if (archivo.exists()) {
                            compartirInforme(context, archivo, informe.codigo)
                        } else {
                            Toast.makeText(
                                context,
                                "El archivo del protocolo ya no está en el dispositivo",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
            }
        }

        // Botón flotante según la sección: empresas o maquinaria
        when (tab) {
            1 -> ExtendedFloatingActionButton(
                onClick = { showAddEmpresa = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Agregar empresa") }
            )
            2 -> ExtendedFloatingActionButton(
                onClick = { showAddMaquina = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Agregar maquinaria") }
            )
        }
    }

    // ---- Diálogos ----
    if (showAddEmpresa) {
        AgregarEmpresaDialog(
            empresa = null,
            onDismiss = { showAddEmpresa = false },
            onConfirm = { nombre, rut ->
                adminViewModel.guardarEmpresa(nombre, rut)
                showAddEmpresa = false
            }
        )
    }

    if (showEditEmpresa) {
        AgregarEmpresaDialog(
            empresa = empresaEditando,
            onDismiss = { showEditEmpresa = false },
            onConfirm = { nombre, rut ->
                empresaEditando?.let { adminViewModel.editarEmpresa(it.id, nombre, rut) }
                showEditEmpresa = false
            }
        )
    }

    if (showAddMaquina) {
        AgregarMaquinaAdminDialog(
            maquina = null,
            sucursales = sucursales,
            onDismiss = { showAddMaquina = false },
            onConfirm = { _id, sucursalId, categoria, codigo ->
                adminViewModel.guardarMaquina(sucursalId, categoria, codigo) {
                    Toast.makeText(
                        context,
                        "Ese código ya existe en esa sucursal",
                        Toast.LENGTH_LONG
                    ).show()
                }
                showAddMaquina = false
            }
        )
    }

    if (showEditMaquina) {
        AgregarMaquinaAdminDialog(
            maquina = maquinaEditando,
            sucursales = sucursales,
            onDismiss = { showEditMaquina = false },
            onConfirm = { id, sucursalId, categoria, codigo ->
                adminViewModel.editarMaquina(id, sucursalId, categoria, codigo)
                showEditMaquina = false
            }
        )
    }

    aEliminar?.let { (tipo, id) ->
        AlertDialog(
            onDismissRequest = { aEliminar = null },
            title = { Text("Eliminar $tipo") },
            text = {
                Text(
                    if (tipo == "Empresa") {
                        "Se eliminará la empresa y todas sus sucursales, maquinaria e informes. ¿Continuar?"
                    } else {
                        "Se eliminará la sucursal y todo su contenido. ¿Continuar?"
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tipo == "Empresa") adminViewModel.eliminarEmpresa(id)
                    else adminViewModel.eliminarSucursal(id)
                    aEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { aEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

// Comparte un protocolo ya emitido usando el FileProvider de la app.
private fun compartirInforme(context: android.content.Context, archivo: File, codigo: String) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Protocolo $codigo - Induztek")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir protocolo PDF"))
}

// ================= SECCIÓN 1: TÉCNICOS =================

@Composable
private fun ListaTecnicos(
    tecnicos: List<Usuario>,
    asignaciones: List<Asignacion>,
    onAprobar: (Long) -> Unit,
    onRechazar: (Long) -> Unit
) {
    val pendientes = tecnicos.count { it.estado == ESTADO_PENDIENTE }

    if (tecnicos.isEmpty()) {
        EstadoVacio(
            icono = Icons.Filled.Person,
            titulo = "Sin técnicos registrados",
            mensaje = "Cuando un técnico cree su cuenta en la app, aparecerá aquí para que puedas aprobarlo."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "resumen") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = if (pendientes > 0) {
                        "$pendientes cuenta(s) esperando aprobación"
                    } else {
                        "Todas las cuentas están gestionadas"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        items(tecnicos, key = { it.id }) { tecnico ->
            TecnicoCard(
                tecnico = tecnico,
                sucursalesAsignadas = asignaciones.count { it.usuarioId == tecnico.id },
                onAprobar = { onAprobar(tecnico.id) },
                onRechazar = { onRechazar(tecnico.id) }
            )
        }
    }
}

@Composable
private fun TecnicoCard(
    tecnico: Usuario,
    sucursalesAsignadas: Int,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Badge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = tecnico.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                EstadoChip(estado = tecnico.estado)
            }

            Spacer(modifier = Modifier.height(8.dp))
            DatoLinea(icon = Icons.Filled.Email, texto = tecnico.email)
            if (tecnico.telefono.isNotBlank()) {
                DatoLinea(icon = Icons.Filled.Phone, texto = tecnico.telefono)
            }
            DatoLinea(
                icon = Icons.Filled.LocationOn,
                texto = "$sucursalesAsignadas sucursal(es) asignada(s)"
            )

            if (tecnico.estado == ESTADO_PENDIENTE) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAprobar, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Aprobar")
                    }
                    OutlinedButton(onClick = onRechazar, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Rechazar")
                    }
                }
            }
        }
    }
}

// ================= SECCIÓN 2: EMPRESAS =================

@Composable
private fun ListaEmpresas(
    empresas: List<Empresa>,
    onNavigateToEmpresa: (Long) -> Unit,
    onEditar: (Long) -> Unit,
    onEliminar: (Long) -> Unit
) {
    if (empresas.isEmpty()) {
        EstadoVacio(
            icono = Icons.Filled.Business,
            titulo = "Sin empresas registradas",
            mensaje = "Agrega la primera empresa cliente para después cargar sus sucursales."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(empresas, key = { it.id }) { empresa ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                    Spacer(modifier = Modifier.size(12.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToEmpresa(empresa.id) }
                    ) {
                        Text(
                            text = empresa.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "RUT ${empresa.rut}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Toca para ver sus sucursales",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onEditar(empresa.id) }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Editar empresa",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onEliminar(empresa.id) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar empresa",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// ================= SECCIÓN 3: MAQUINARIA PENDIENTE =================

@Composable
private fun ListaMaquinas(
    maquinas: List<Maquina>,
    sucursales: List<Sucursal>,
    onAprobar: (Long) -> Unit,
    onRechazar: (Long) -> Unit,
    onEditar: (Long) -> Unit
) {
    if (maquinas.isEmpty()) {
        EstadoVacio(
            icono = Icons.Filled.Check,
            titulo = "Sin maquinaria pendiente",
            mensaje = "Los equipos que propongan los técnicos en terreno aparecerán aquí para su validación."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(maquinas, key = { it.id }) { maquina ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${maquina.categoria} · ${maquina.codigo}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = sucursales.firstOrNull { it.id == maquina.sucursalId }?.nombre
                                    .orEmpty(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        EstadoChip(estado = maquina.estado)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { onEditar(maquina.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Editar maquinaria",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(onClick = { onAprobar(maquina.id) }, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Aprobar")
                        }
                        OutlinedButton(onClick = { onRechazar(maquina.id) }, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Rechazar")
                        }
                    }
                }
            }
        }
    }
}

// ================= SECCIÓN 4: HISTORIAL DE INFORMES =================

@Composable
private fun ListaInformes(
    informes: List<Informe>,
    empresas: List<Empresa>,
    sucursales: List<Sucursal>,
    onCompartir: (Informe) -> Unit
) {
    if (informes.isEmpty()) {
        EstadoVacio(
            icono = Icons.Filled.Description,
            titulo = "Sin protocolos emitidos",
            mensaje = "Cuando un técnico genere un protocolo en terreno, quedará registrado acá."
        )
        return
    }

    // Ordenado por empresa y, dentro de cada una, por sucursal.
    val porEmpresa = informes.groupBy { it.empresaId }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        porEmpresa.forEach { (empresaId, lista) ->
            val empresa = empresas.firstOrNull { it.id == empresaId }
            item(key = "empresa_$empresaId") {
                Text(
                    text = empresa?.nombre ?: "Empresa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            lista.groupBy { it.sucursalId }.forEach { (sucursalId, deSucursal) ->
                val sucursal = sucursales.firstOrNull { it.id == sucursalId }
                item(key = "sucursal_${empresaId}_$sucursalId") {
                    Text(
                        text = "   ${sucursal?.nombre ?: "Sucursal"}",
                        style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(deSucursal, key = { it.id }) { informe ->
                    InformeCard(informe = informe, onCompartir = { onCompartir(informe) })
                }
            }
        }
    }
}

@Composable
private fun InformeCard(
    informe: Informe,
    onCompartir: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${informe.categoria} · ${informe.codigo}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = informe.fechaHora,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = onCompartir) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("PDF")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = informe.valores,
                style = MaterialTheme.typography.bodyMedium
            )
            if (informe.precio != null) {
                Text(
                    text = "Valor: ${formatearPrecio(informe.precio)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

// ================= DIÁLOGOS =================

@Composable
private fun AgregarEmpresaDialog(
    empresa: Empresa? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var nombre by remember { mutableStateOf(empresa?.nombre.orEmpty()) }
    var rut by remember { mutableStateOf(empresa?.rut.orEmpty()) }

    val nombreError = Validaciones.empresaNombre(nombre).takeIf { nombre.isNotBlank() }
    val rutError = Validaciones.rut(rut).takeIf { rut.isNotBlank() }
    val esValido = Validaciones.empresaNombre(nombre) == null && Validaciones.rut(rut) == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (empresa == null) "Nueva empresa cliente" else "Editar empresa ${empresa.nombre}") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre empresa *") },
                    placeholder = { Text("Ej: Jumbo") },
                    singleLine = true,
                    isError = nombreError != null,
                    supportingText = { nombreError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rut,
                    onValueChange = { rut = it },
                    label = { Text("RUT *") },
                    placeholder = { Text("Ej: 76.123.456-7") },
                    singleLine = true,
                    isError = rutError != null,
                    supportingText = { rutError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(nombre.trim(), rut.trim()) }, enabled = esValido) {
                Text(if (empresa == null) "Agregar" else "Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun AgregarMaquinaAdminDialog(
    maquina: Maquina? = null,
    sucursales: List<Sucursal>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long, String, String) -> Unit
) {
    var sucursalId by remember { mutableStateOf(maquina?.sucursalId ?: (sucursales.firstOrNull()?.id ?: 0L)) }
    var categoria by remember { mutableStateOf(maquina?.categoria ?: categoriasMaquina.first().nombre) }
    var codigo by remember { mutableStateOf(maquina?.codigo.orEmpty()) }
    var expandedSucursal by remember { mutableStateOf(false) }
    var expandedCategoria by remember { mutableStateOf(false) }

    val codigoError = Validaciones.codigoMaquina(codigo).takeIf { codigo.isNotBlank() }
    val esValido = sucursalId > 0L && Validaciones.codigoMaquina(codigo) == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (maquina == null) "Registrar maquinaria" else "Editar maquinaria ${maquina.codigo}") },
        text = {
            Column {
                Box {
                    OutlinedTextField(
                        value = sucursales.firstOrNull { it.id == sucursalId }?.nombre.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sucursal *") },
                        trailingIcon = {
                            TextButton(onClick = { expandedSucursal = !expandedSucursal }) {
                                Text(if (expandedSucursal) "▲" else "▼")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expandedSucursal,
                        onDismissRequest = { expandedSucursal = false }
                    ) {
                        sucursales.forEach { sucursal ->
                            DropdownMenuItem(
                                text = { Text(sucursal.nombre) },
                                onClick = {
                                    sucursalId = sucursal.id
                                    expandedSucursal = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría *") },
                        trailingIcon = {
                            TextButton(onClick = { expandedCategoria = !expandedCategoria }) {
                                Text(if (expandedCategoria) "▲" else "▼")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expandedCategoria,
                        onDismissRequest = { expandedCategoria = false }
                    ) {
                        categoriasMaquina.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.nombre) },
                                onClick = {
                                    categoria = cat.nombre
                                    expandedCategoria = false
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(maquina?.id ?: -1L, sucursalId, categoria, codigo.trim()) },
                enabled = esValido
            ) { Text(if (maquina == null) "Registrar" else "Guardar cambios") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// ================= ELEMENTOS COMPARTIDOS =================

@Composable
private fun EstadoVacio(icono: ImageVector, titulo: String, mensaje: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DatoLinea(icon: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Distintivo visual del estado de aprobación.
@Composable
private fun EstadoChip(estado: String) {
    val (texto, fondo) = when (estado) {
        ESTADO_APROBADO -> "Aprobado" to MaterialTheme.colorScheme.primaryContainer
        ESTADO_RECHAZADO -> "Rechazado" to MaterialTheme.colorScheme.errorContainer
        else -> "Pendiente" to MaterialTheme.colorScheme.tertiaryContainer
    }
    Surface(color = fondo, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Unspecified,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
