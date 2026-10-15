package com.example.appmovil.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.appmovil.data.local.maquina.MaquinaEntity
import com.example.appmovil.data.local.medicion.MedicionEntity
import com.example.appmovil.domain.validation.validateMedicion
import com.example.appmovil.viewmodel.MaquinaViewModel
import com.example.appmovil.viewmodel.MedicionViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Patrón Ej.8 ProfileScreen cámara: archivo temporal + FileProvider + TakePicture.
private fun createTempRefFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
    val file = File(storageDir, "REF_$timeStamp.jpg")
    if (!file.exists()) file.createNewFile()
    return file
}

// Formulario de medición y detalle de máquina (CasoA pantalla 5).
// - Muestra historial de pruebas anteriores de Room.
// - Formulario de valores + observaciones.
// - Si es el primer equipo de la categoría en la empresa, pide foto de referencia;
//   si no, reutiliza la foto de la familia automáticamente.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicionFormScreen(
    maquinaId: Long,
    maquinaViewModel: MaquinaViewModel,
    medicionViewModel: MedicionViewModel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onMedicionGuardada: (Long) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(maquinaId) { medicionViewModel.observarMaquina(maquinaId) }
    val mediciones by medicionViewModel.mediciones.collectAsStateWithLifecycle()
    val maquinasEmpresa by maquinaViewModel.maquinas.collectAsStateWithLifecycle()

    var maquina by remember(maquinaId) { mutableStateOf<MaquinaEntity?>(null) }
    LaunchedEffect(maquinaId, maquinasEmpresa) {
        maquina = maquinasEmpresa.find { it.id == maquinaId }
    }

    var valores by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var fotoRef by rememberSaveable(maquina) { mutableStateOf(maquina?.fotoRefUri.orEmpty()) }
    var pendingCapture by rememberSaveable { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    var ultimaGuardadaId by remember { mutableStateOf(0L) }
    var yaGuardado by remember { mutableStateOf(false) }

    LaunchedEffect(maquina) { fotoRef = maquina?.fotoRefUri.orEmpty() }

    // ¿Es el primer equipo de su categoría en la empresa? Si hay otra máquina de la
    // misma categoría con foto, se reutiliza; si esta es la primera, se exige foto.
    val esPrimeraDeCategoria = remember(maquinasEmpresa, maquina) {
        val mismaCategoria = maquinasEmpresa.filter {
            it.categoria == maquina?.categoria && it.id != maquina?.id
        }
        mismaCategoria.none { it.fotoRefUri.isNotBlank() } && maquina?.fotoRefUri.isNullOrBlank()
    }
    val fotoReutilizada = remember(maquinasEmpresa, maquina) {
        maquinasEmpresa.firstOrNull {
            it.categoria == maquina?.categoria && it.fotoRefUri.isNotBlank() && it.id != maquina?.id
        }?.fotoRefUri.orEmpty()
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCapture?.let { fotoRef = it }
            Toast.makeText(context, "Foto de referencia capturada", Toast.LENGTH_SHORT).show()
        }
        pendingCapture = null
    }

    fun launchCamera() {
        try {
            val file = createTempRefFile(context)
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            pendingCapture = uri.toString()
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            pendingCapture = null
            Toast.makeText(context, "No se pudo abrir la cámara: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_LONG).show()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { /* acceso temporal */ }
            fotoRef = it.toString()
            Toast.makeText(context, "Foto de referencia seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    val medicionError = validateMedicion(valores)
    val requiereFoto = esPrimeraDeCategoria && fotoRef.isBlank()
    val esValido = valores.isNotBlank() && medicionError == null && !requiereFoto && !yaGuardado

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(maquina?.codigo ?: "Medición en terreno")
                        Text(
                            text = maquina?.categoria.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
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
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Historial de pruebas anteriores (Room).
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
                        HistorialCard(med)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Foto de referencia de la familia.
            if (esPrimeraDeCategoria) {
                Text(
                    text = "Foto de referencia de la familia ${maquina?.categoria.orEmpty()} *",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                if (fotoRef.isNotBlank()) {
                    AsyncImage(
                        model = fotoRef,
                        contentDescription = "Foto de referencia",
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                                PackageManager.PERMISSION_GRANTED
                            ) launchCamera()
                            else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
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
                        text = "Captura o adjunta la foto que se usará para toda la familia de equipos.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else if (fotoReutilizada.isNotBlank() || fotoRef.isNotBlank()) {
                val fotoMostrar = fotoRef.ifBlank { fotoReutilizada }
                Text(
                    text = "Foto de referencia reutilizada de la familia",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth()
                )
                AsyncImage(
                    model = fotoMostrar,
                    contentDescription = "Foto referencia reutilizada",
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

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
                    if (yaGuardado) return@Button
                    val err = validateMedicion(valores)
                    if (err != null || requiereFoto) return@Button
                    yaGuardado = true
                    triggerHapticFeedback(context)
                    // Si capturó foto en la primera de la categoría, guardarla como referencia.
                    if (m.fotoRefUri.isBlank() && fotoRef.isNotBlank()) {
                        maquinaViewModel.completarFotoRef(m, fotoRef)
                    }
                    val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    medicionViewModel.guardarMedicion(
                        maquinaId = m.id,
                        valores = valores,
                        observaciones = observaciones,
                        fechaHora = fechaActual
                    ) { nuevoId ->
                        ultimaGuardadaId = nuevoId
                        valores = ""
                        observaciones = ""
                        showSuccess = true
                    }
                },
                enabled = esValido,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar medición en terreno")
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

@Composable
private fun HistorialCard(med: MedicionEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = med.fechaHora,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = med.valores, style = MaterialTheme.typography.bodyMedium)
            if (med.observaciones.isNotBlank()) {
                Text(
                    text = med.observaciones,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
