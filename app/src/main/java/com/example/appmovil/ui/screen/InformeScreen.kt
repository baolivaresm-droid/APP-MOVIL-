package com.example.appmovil.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.appmovil.data.local.pdf.InformePdf
import com.example.appmovil.viewmodel.MaquinaViewModel
import com.example.appmovil.viewmodel.MedicionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// Informe y exportación PDF (CasoA pantalla 6).
// Cruza la medición actual con la última lectura histórica de Room
// en una tabla comparativa + botón Generar y Compartir PDF.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformeScreen(
    medicionId: Long,
    medicionViewModel: MedicionViewModel,
    maquinaViewModel: MaquinaViewModel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit
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
    var previaValores by remember { mutableStateOf<String?>(null) }
    var previaFecha by remember { mutableStateOf<String?>(null) }
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var generando by remember { mutableStateOf(false) }

    LaunchedEffect(medicionId) {
        try {
            val actual = withContext(Dispatchers.IO) {
                medicionViewModel.getById(medicionId)
            } ?: throw IllegalArgumentException("Medición no encontrada")
            val historial = withContext(Dispatchers.IO) {
                medicionViewModel.historialDe(actual.maquinaId)
            }
            val previa = historial.firstOrNull { it.id != actual.id }
            val maquina = withContext(Dispatchers.IO) {
                maquinaViewModel.getById(actual.maquinaId)
            }
            codigo = maquina?.codigo.orEmpty()
            categoria = maquina?.categoria.orEmpty()
            actualValores = actual.valores
            actualObs = actual.observaciones
            actualFecha = actual.fechaHora
            previaValores = previa?.valores
            previaFecha = previa?.fechaHora
            if (maquina != null) maquinaViewModel.observarEmpresa(maquina.empresaId)
        } catch (e: Exception) {
            error = e.message ?: "No se pudo cargar el informe"
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Informe del protocolo")
                        if (codigo.isNotBlank()) {
                            Text(
                                text = codigo,
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
        }
    ) { innerPadding ->
        when {
            cargando -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
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
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
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
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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

                    Button(
                        onClick = {
                            if (generando) return@Button
                            generando = true
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val file = InformePdf.generar(
                                        context = context,
                                        empresaNombre = "",
                                        empresaZona = "",
                                        categoria = categoria,
                                        codigo = codigo,
                                        actualValores = actualValores,
                                        actualObs = actualObs,
                                        actualFecha = actualFecha,
                                        previaValores = previaValores,
                                        previaFecha = previaFecha
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
                        enabled = !generando,
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
