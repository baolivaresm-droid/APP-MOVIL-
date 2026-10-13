package com.example.appmovil.ui.screen

import android.content.Context
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt // O podemos usar List o Checklist
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmovil.domain.validation.validateMedicion
import com.example.appmovil.model.EquipoElectrico
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    equipoId: Int,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onNavigateToProtocolos: () -> Unit,
    onAddProtocol: (EquipoElectrico, String, String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val product = buscarEquipo(equipoId)

    var valoresMedicion by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    // Evita el doble tap: una vez guardado, el botón queda deshabilitado hasta salir del diálogo.
    var yaGuardado by remember { mutableStateOf(false) }

    // Ej.5: animar botón guardar según validez del formulario
    val medicionError = validateMedicion(valoresMedicion)
    val esValido = valoresMedicion.isNotBlank() && medicionError == null
    val saveColor by animateColorAsState(
        targetValue = if (esValido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 500),
        label = "saveColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Protocolo de Ensayo en Terreno") },
                navigationIcon = {
                    // Flecha izquierda: Vuelve atrás (edición / catálogo)
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Ícono derecho 1: Ir directo a los protocolos registrados
                    IconButton(onClick = onNavigateToProtocolos) {
                        Icon(imageVector = Icons.Default.ListAlt, contentDescription = "Ver Protocolos")
                    }
                    // Ícono derecho 2: Ir directo al Inicio (Home)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Image(
                    painter = painterResource(id = product.imagenResId),
                    contentDescription = product.titulo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = product.titulo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Código ID: ${product.codigoEquipo}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📍 Ubicación: ${product.ubicacionCliente}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📊 Historial: ${product.resultadosAnteriores}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = product.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = valoresMedicion,
                onValueChange = {
                    valoresMedicion = it
                    errorMensaje = null
                },
                label = { Text("Ingrese valores de medición actuales *") },
                placeholder = { Text("Ej: Aislamiento 2.8 GΩ / Disparo 5.0 A") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                isError = errorMensaje != null,
                supportingText = {
                    errorMensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                leadingIcon = { Icon(Icons.Filled.Speed, contentDescription = "Medición") }
            )

            AnimatedVisibility(visible = esValido) {
                Text(
                    text = "Medición lista para guardar",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (yaGuardado) return@Button
                    val err = validateMedicion(valoresMedicion)
                    if (err != null) {
                        errorMensaje = err
                        return@Button
                    }
                    yaGuardado = true
                    triggerHapticFeedback(context)

                    val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    onAddProtocol(product, valoresMedicion, fechaActual)
                    valoresMedicion = ""
                    showSuccessDialog = true
                },
                enabled = !yaGuardado,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = saveColor)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Guardar Protocolo en Terreno", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("¡Registro exitoso!") },
            text = { Text("El protocolo quedó guardado. ¿Qué deseas hacer?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateToProtocolos()
                    }
                ) {
                    Text("Ver items a sincronizar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSuccessDialog = false
                        onHomeClick()
                    }
                ) {
                    Text("Volver a los equipos")
                }
            }
        )
    }
}

// Nombre del caso Induztek (wrapper del nombre anterior).
@Composable
fun EquipoDetailScreen(
    equipoId: Int,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onNavigateToProtocolos: () -> Unit,
    onAddProtocol: (EquipoElectrico, String, String) -> Unit
) = ProductDetailScreen(equipoId, onBackClick, onHomeClick, onNavigateToProtocolos, onAddProtocol)

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