package com.example.appmovil.ui

import android.content.Context
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt // O podemos usar List o Checklist
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmovil.models.Product
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.appmovil.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onNavigateToCart: () -> Unit,
    onAddProtocol: (Product, String, String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val sampleProducts = listOf(
        Product(1, "Transformador de Potencia 220kV", "TR-220-01", "Subestación El Salto (Cliente: Minera Norte)", "Resistencia aislamiento anterior: 2.5 GΩ (Año previo)", "Prueba de resistencia de aislamiento, relación de transformación y tangente delta.", R.drawable.transformador),
        Product(2, "Relé de Protección Sepam", "REL-SEP-04", "Sala eléctrica Planta Industrial Maipú", "Umbral de disparo anterior: 5.2 A / 0.4s", "Prueba de inyección de corrientes secundarias y verificación de umbrales.", R.drawable.rele),
        Product(3, "Celda de Media Tensión 12kV", "CEL-12V-09", "Centro de Distribución Quilicura", "Resistencia de contactos anterior: 45 µΩ", "Inspección de sistemas de enclavamiento y resistencia de contactos.", R.drawable.celda),
        Product(4, "Banco de Baterías Subestación", "BAT-SUB-02", "Subestación Lo Espejo", "Voltaje promedio por celda anterior: 2.15 V", "Prueba de descarga y medición de voltaje por celda y resistencia interna.", R.drawable.baterias)
    )

    val product = sampleProducts.find { it.id == productId } ?: sampleProducts.first()

    var valoresMedicion by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

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
                    IconButton(onClick = onNavigateToCart) {
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
                minLines = 2
            )

            errorMensaje?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (valoresMedicion.isBlank()) {
                        errorMensaje = "Debe registrar los valores de medición para guardar el protocolo"
                        return@Button
                    }
                    triggerHapticFeedback(context)

                    val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    onAddProtocol(product, valoresMedicion, fechaActual)
                    showSuccessDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
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
            title = { Text("¡Protocolo Guardado con Éxito!") },
            text = { Text("¿Qué deseas hacer a continuación?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateToCart()
                    }
                ) {
                    Text("Ver Protocolos Registrados")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSuccessDialog = false
                        onHomeClick()
                    }
                ) {
                    Text("Volver a Equipos Eléctricos")
                }
            }
        )
    }
}

fun triggerHapticFeedback(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
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