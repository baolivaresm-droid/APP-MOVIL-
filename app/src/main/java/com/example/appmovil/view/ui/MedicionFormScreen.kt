package com.example.appmovil.view.ui

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.appmovil.repository.models.Maquina
import com.example.appmovil.repository.models.Medicion
import com.example.appmovil.view.utils.CameraCapture
import com.example.appmovil.viewmodels.MaquinaViewModel
import com.example.appmovil.viewmodels.MedicionViewModel
import com.example.appmovil.view.utils.AnimacionEntradaFormulario
import com.example.appmovil.viewmodels.Validaciones
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Formulario de medición y detalle de máquina.
// - Muestra el historial de pruebas anteriores de Room.
// - Registra valores, observaciones y el valor del trabajo (opcional).
// - Exige la foto del equipo como evidencia para el protocolo PDF.
@Composable
fun MedicionFormScreen(
    maquinaId: Long,
    maquinaViewModel: MaquinaViewModel,
    medicionViewModel: MedicionViewModel,
    onBackClick: () -> Unit,
    onMedicionGuardada: (Long) -> Unit,
    onNavigateToInforme: (Long) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(maquinaId) { medicionViewModel.observarMaquina(maquinaId) }
    val mediciones by medicionViewModel.mediciones.collectAsStateWithLifecycle()
    val maquinasEmpresa by maquinaViewModel.maquinas.collectAsStateWithLifecycle()

    var maquina by remember(maquinaId) { mutableStateOf<Maquina?>(null) }
    LaunchedEffect(maquinaId, maquinasEmpresa) {
        maquina = maquinasEmpresa.find { it.id == maquinaId }
    }

    var valores by rememberSaveable { mutableStateOf("") }
    var observaciones by rememberSaveable { mutableStateOf("") }
    var precio by rememberSaveable { mutableStateOf("") }
    // Foto de evidencia del equipo al momento de esta medición: se adjunta al
    // protocolo PDF como respaldo del trabajo ejecutado.
    var fotoEvidencia by rememberSaveable(maquinaId) { mutableStateOf("") }
    var showCamera by rememberSaveable { mutableStateOf(false) }
    // ANIMACIÓN 1 (view/utils/Animations.kt): entrada del formulario
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    var showSuccess by remember { mutableStateOf(false) }
    var ultimaGuardadaId by remember { mutableStateOf(0L) }
    var yaGuardado by remember { mutableStateOf(false) }
    // Medición que se está editando (modo "corrección" del técnico); null = crear nueva.
    var editandoMedicion by remember { mutableStateOf<Medicion?>(null) }

    // Edición de la medición vigente: el técnico corrige el valor antes de que la
    // prueba quede consolidada como histórica (evita lecturas corruptas en el informe).
    val iniciarEdicion = { med: Medicion ->
        editandoMedicion = med
        valores = med.valores
        observaciones = med.observaciones
        precio = med.precio?.toString().orEmpty()
        yaGuardado = false
    }
    val cancelarEdicion = {
        editandoMedicion = null
        valores = ""
        observaciones = ""
        precio = ""
        yaGuardado = false
    }

    // Galería moderna (PhotoPicker): alternativa a la cámara para adjuntar la foto
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            fotoEvidencia = it.toString()
            Toast.makeText(context, "Foto de evidencia seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    val medicionError = Validaciones.medicion(valores)
    val precioError = Validaciones.precio(precio)
    // La foto del equipo es obligatoria en cada medición: es la evidencia de cómo
    // estaba el equipo en el momento de la prueba.
    val requiereFoto = fotoEvidencia.isBlank()
    val esValido = if (editandoMedicion != null) {
        medicionError == null && !requiereFoto
    } else {
        valores.isNotBlank() && medicionError == null && !requiereFoto && !yaGuardado
    }

    // ANIMACIÓN 1 (view/utils/Animations.kt): la cámara se abre a pantalla completa
    if (showCamera) {
        CameraCapture(
            carpeta = "mediciones",
            onImageCaptured = { uri ->
                fotoEvidencia = uri.toString()
                showCamera = false
                Toast.makeText(context, "Foto de evidencia capturada", Toast.LENGTH_SHORT).show()
            },
            onClose = { showCamera = false }
        )
        return
    }

    // ANIMACIÓN 1 (view/utils/Animations.kt): entrada del formulario
    AnimacionEntradaFormulario(visible = visible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado del equipo que se está midiendo
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = maquina?.codigo ?: "Medición en terreno",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = maquina?.categoria.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Historial de pruebas anteriores (Room)
            Text(
                text = "Historial de pruebas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (mediciones.isEmpty()) {
                Text(
                    text = "Primera medición de este equipo: no hay historial previo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(mediciones, key = { it.id }) { med ->
                        HistorialCard(
                            med = med,
                            onClick = { onNavigateToInforme(med.id) },
                            onEditar = { iniciarEdicion(med) },
                            editable = med.id == mediciones.firstOrNull()?.id
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Foto de evidencia del equipo en esta medición
            Text(
                text = "Foto del equipo al momento de la medición *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            if (fotoEvidencia.isNotBlank()) {
                AsyncImage(
                    model = fotoEvidencia,
                    contentDescription = "Foto de evidencia del equipo",
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showCamera = true },
                    modifier = Modifier.weight(1f)
                ) { Text("Cámara") }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Galería") }
            }
            if (requiereFoto) {
                Text(
                    text = "Toma o adjunta la foto del equipo: es la evidencia de cómo estaba al momento de la prueba.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "La foto se adjunta como evidencia al protocolo PDF.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = valores,
                onValueChange = { valores = it },
                label = { Text("Valores de la medición actual *") },
                placeholder = { Text("Ej: Aislamiento 2.8 GΩ / Disparo 5.0 A") },
                minLines = 2,
                isError = medicionError != null && valores.isNotBlank(),
                supportingText = {
                    if (valores.isNotBlank()) medicionError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                leadingIcon = { Icon(Icons.Filled.Speed, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones") },
                placeholder = { Text("Ej: Equipo en buen estado, sin hallazgos") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Precio del trabajo: es opcional y se imprime en el protocolo PDF
            OutlinedTextField(
                value = precio,
                onValueChange = { nuevo ->
                    precio = nuevo.filter { it.isDigit() || it == '.' || it == ',' }
                },
                label = { Text("Valor del trabajo (opcional)") },
                placeholder = { Text("Ej: 85000") },
                prefix = { Text("$ ") },
                singleLine = true,
                isError = precioError != null,
                supportingText = {
                    if (precioError != null) {
                        Text(precioError, color = MaterialTheme.colorScheme.error)
                    }
                },
                leadingIcon = { Icon(Icons.Filled.Payments, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            // ANIMACIÓN 1 (view/utils/Animations.kt): mensaje de formulario listo
            AnimatedVisibility(visible = esValido) {
                Text(
                    text = "Medición lista para guardar",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val m = maquina ?: return@Button
                    if (medicionError != null || requiereFoto) return@Button
                    val precioNum = precio.trim().replace(",", ".").toDoubleOrNull()
                    triggerHapticFeedback(context)
                    // La foto viaja en memoria hasta el informe: la app no la guarda.
                    medicionViewModel.setFotoEvidencia(fotoEvidencia)
                    val editando = editandoMedicion
                    if (editando != null) {
                        // Modo edición: se corrige la medición vigente sin crear una nueva.
                        medicionViewModel.editarMedicion(
                            medicionId = editando.id,
                            valores = valores,
                            observaciones = observaciones,
                            fechaHora = editando.fechaHora,
                            precio = precioNum
                        )
                        ultimaGuardadaId = editando.id
                        editandoMedicion = null
                        cancelarEdicion()
                        showSuccess = true
                    } else {
                        if (yaGuardado) return@Button
                        yaGuardado = true
                        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                        medicionViewModel.guardarMedicion(
                            maquinaId = m.id,
                            valores = valores,
                            observaciones = observaciones,
                            fechaHora = fechaActual,
                            precio = precioNum
                        ) { nuevoId ->
                            ultimaGuardadaId = nuevoId
                            valores = ""
                            observaciones = ""
                            precio = ""
                            showSuccess = true
                        }
                    }
                },
                enabled = esValido,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (editandoMedicion != null) "Guardar cambios" else "Guardar medición en terreno")
            }
        }
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("¡Medición guardada!") },
            text = { Text("¿Deseas ver el informe comparativo con la lectura anterior?") },
            confirmButton = {
                Button(onClick = {
                    showSuccess = false
                    onMedicionGuardada(ultimaGuardadaId)
                }) { Text("Ver informe") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showSuccess = false
                    yaGuardado = false
                    onBackClick()
                }) { Text("Volver") }
            }
        )
    }
}

// Formatea el valor del trabajo para mostrarlo en pantalla y en el protocolo
fun formatearPrecio(valor: Double): String =
    "$" + String.format(Locale.getDefault(), "%,.0f", valor)

@Composable
private fun HistorialCard(
    med: Medicion,
    onClick: () -> Unit,
    onEditar: (Long) -> Unit = {},
    editable: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = med.fechaHora,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = med.valores, style = MaterialTheme.typography.bodyMedium)
                    if (med.precio != null) {
                        Text(
                            text = "Valor del trabajo: ${formatearPrecio(med.precio)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (med.observaciones.isNotBlank()) {
                        Text(
                            text = med.observaciones,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (editable) {
                    IconButton(onClick = { onEditar(med.id) }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Editar medición",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Text(
                text = "Toca para ver el informe",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

fun triggerHapticFeedback(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(100)
    }
}
