package com.example.appmovil.view.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appmovil.view.utils.InformePdf
import com.example.appmovil.repository.models.Usuario
import com.example.appmovil.repository.models.ESTADO_APROBADO
import com.example.appmovil.viewmodels.EmpresaViewModel
import com.example.appmovil.viewmodels.InformeViewModel
import com.example.appmovil.viewmodels.MaquinaViewModel
import com.example.appmovil.viewmodels.MedicionViewModel
import com.example.appmovil.viewmodels.SucursalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// Informe y exportación PDF (CasoA pantalla 6).
// Cruza la medición actual con la última lectura histórica de Room
// en una tabla comparativa + botón Generar y Compartir PDF.
@Composable
fun InformeScreen(
    medicionId: Long,
    medicionViewModel: MedicionViewModel,
    maquinaViewModel: MaquinaViewModel,
    sucursalViewModel: SucursalViewModel,
    empresaViewModel: EmpresaViewModel,
    informeViewModel: InformeViewModel,
    tecnico: Usuario?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var codigo by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var actualValores by remember { mutableStateOf("") }
    var actualObs by remember { mutableStateOf("") }
    var actualFecha by remember { mutableStateOf("") }
    var actualPrecio by remember { mutableStateOf<Double?>(null) }
    var previaValores by remember { mutableStateOf<String?>(null) }
    var previaFecha by remember { mutableStateOf<String?>(null) }
    var empresaNombre by remember { mutableStateOf("") }
    var empresaRut by remember { mutableStateOf("") }
    var sucursalId by remember { mutableStateOf(0L) }
    var empresaId by remember { mutableStateOf(0L) }
    var maquinaId by remember { mutableStateOf(0L) }
    var sucursalNombre by remember { mutableStateOf("") }
    var sucursalDireccion by remember { mutableStateOf("") }
    var sucursalComuna by remember { mutableStateOf("") }
    var sucursalTelefono by remember { mutableStateOf("") }
    var maquinaPendiente by remember { mutableStateOf(false) }
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var generando by remember { mutableStateOf(false) }
    // Foto de evidencia: vive en memoria (el ViewModel) mientras se arma el protocolo.
    val fotoEvidencia by medicionViewModel.fotoEvidencia.collectAsStateWithLifecycle()

    LaunchedEffect(medicionId) {
        try {
            val actual = withContext(Dispatchers.IO) {
                medicionViewModel.getMedicion(medicionId)
            } ?: throw IllegalArgumentException("Medición no encontrada")
            val historial = withContext(Dispatchers.IO) {
                medicionViewModel.historialDe(actual.maquinaId)
            }
            val previa = historial.firstOrNull { it.id != actual.id }
            val maquina = withContext(Dispatchers.IO) {
                maquinaViewModel.getMaquina(actual.maquinaId)
            }
            codigo = maquina?.codigo.orEmpty()
            categoria = maquina?.categoria.orEmpty()
            maquinaId = actual.maquinaId
            maquinaPendiente = maquina != null && maquina.estado != ESTADO_APROBADO
            actualValores = actual.valores
            actualObs = actual.observaciones
            actualFecha = actual.fechaHora
            actualPrecio = actual.precio
            previaValores = previa?.valores
            previaFecha = previa?.fechaHora

            // Datos de la ubicación y del cliente: el protocolo debe identificar
            // en qué instalación se hizo la prueba.
            val sucursal = maquina?.let { m ->
                withContext(Dispatchers.IO) { sucursalViewModel.getSucursal(m.sucursalId) }
            }
            sucursalId = sucursal?.id ?: 0L
            sucursalNombre = sucursal?.nombre.orEmpty()
            sucursalDireccion = sucursal?.direccion.orEmpty()
            sucursalComuna = sucursal?.comuna.orEmpty()
            sucursalTelefono = sucursal?.telefono.orEmpty()

            val empresa = sucursal?.let { s ->
                withContext(Dispatchers.IO) { empresaViewModel.getEmpresa(s.empresaId) }
            }
            empresaId = empresa?.id ?: 0L
            empresaNombre = empresa?.nombre.orEmpty()
            empresaRut = empresa?.rut.orEmpty()
        } catch (e: Exception) {
            error = e.message ?: "No se pudo cargar el informe"
        } finally {
            cargando = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            cargando -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Cargando informe...")
                }
            }
            error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = onBackClick) { Text("Volver") }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Encabezado del protocolo (el título global lo pone el drawer)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Informe del protocolo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (codigo.isNotBlank()) {
                            Text(
                                text = codigo,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "$categoria - $codigo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Medición actual: $actualFecha",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Datos que CHIP/informa identifica en el protocolo.
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Datos del protocolo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            DatoProtocolo(
                                icon = Icons.Filled.Business,
                                titulo = "Cliente",
                                valor = "${empresaNombre.ifBlank { "Sin empresa" }}${if (empresaRut.isNotBlank()) " · RUT $empresaRut" else ""}"
                            )
                            DatoProtocolo(
                                icon = Icons.Filled.LocationOn,
                                titulo = "Sucursal",
                                valor = "${sucursalNombre.ifBlank { "Sin sucursal" }} · ${sucursalDireccion}, ${sucursalComuna}"
                            )
                            DatoProtocolo(
                                icon = Icons.Filled.Person,
                                titulo = "Técnico responsable",
                                valor = tecnico?.nombre.orEmpty()
                            )
                            if (actualPrecio != null) {
                                DatoProtocolo(
                                    icon = Icons.Filled.Payments,
                                    titulo = "Valor del trabajo",
                                    valor = formatearPrecio(actualPrecio!!)
                                )
                            }
                        }
                    }

                    // Tabla comparativa actual vs última histórica.
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Tabla comparativa",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TablaFila("Lectura", "Valores", header = true)
                            HorizontalDivider()
                            if (previaValores != null && previaFecha != null) {
                                TablaFila("Anterior\n$previaFecha", previaValores!!)
                                HorizontalDivider()
                            } else {
                                TablaFila("Anterior", "Sin lectura previa (primera medición)")
                                HorizontalDivider()
                            }
                            TablaFila("Actual\n$actualFecha", actualValores)
                            if (actualObs.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Observaciones: $actualObs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // La maquinaria propuesta por el técnico aún no fue validada por el
                    // administrador: los datos quedan guardados, pero el protocolo
                    // formal se emite recién con la aprobación.
                    if (maquinaPendiente) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Text(
                                text = "La maquinaria $codigo está pendiente de validación del administrador. " +
                                    "La medición ya quedó guardada y podrás emitir el protocolo apenas sea aprobada.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (generando) return@Button
                            if (maquinaPendiente) {
                                Toast.makeText(
                                    context,
                                    "La maquinaria aún no está aprobada por el administrador.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }
                            generando = true
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val file = InformePdf.generar(
                                        context = context,
                                        datos = InformePdf.DatosInforme(
                                            empresaNombre = empresaNombre,
                                            empresaRut = empresaRut,
                                            sucursalNombre = sucursalNombre,
                                            sucursalDireccion = sucursalDireccion,
                                            sucursalComuna = sucursalComuna,
                                            sucursalTelefono = sucursalTelefono,
                                            tecnicoNombre = tecnico?.nombre.orEmpty(),
                                            tecnicoEmail = tecnico?.email.orEmpty(),
                                            categoria = categoria,
                                            codigo = codigo,
                                            actualValores = actualValores,
                                            actualObs = actualObs,
                                            actualFecha = actualFecha,
                                            actualPrecio = actualPrecio,
                                            previaValores = previaValores,
                                            previaFecha = previaFecha,
                                            fotoEvidencia = fotoEvidencia
                                        )
                                    )
                                    // El protocolo emitido queda en el historial del
                                    // administrador, ordenado por empresa y sucursal.
                                    informeViewModel.registrar(
                                        empresaId = empresaId,
                                        sucursalId = sucursalId,
                                        maquinaId = maquinaId,
                                        usuarioId = tecnico?.id ?: 0L,
                                        codigo = codigo,
                                        categoria = categoria,
                                        valores = actualValores,
                                        observaciones = actualObs,
                                        precio = actualPrecio,
                                        fechaHora = actualFecha,
                                        pdfPath = file.absolutePath
                                    )
                                    withContext(Dispatchers.Main) {
                                        pdfFile = file
                                        generando = false
                                        InformePdf.compartir(context, file, codigo)
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        generando = false
                                        Toast.makeText(
                                            context,
                                            "No se pudo generar el PDF: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        },
                        enabled = !generando && !maquinaPendiente,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(if (generando) "Generando PDF..." else "Generar y Compartir PDF")
                    }

                    if (pdfFile != null) {
                        OutlinedButton(
                            onClick = { pdfFile?.let { InformePdf.compartir(context, it, codigo) } },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Compartir nuevamente")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatoProtocolo(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(18.dp)
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Column {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = valor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TablaFila(primera: String, segunda: String, header: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = primera,
            style = if (header) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (header) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = segunda,
            style = if (header) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1.5f)
        )
    }
}
