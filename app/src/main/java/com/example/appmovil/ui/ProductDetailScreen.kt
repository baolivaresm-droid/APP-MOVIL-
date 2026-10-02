package com.example.appmovil.ui

import android.content.Context
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAddToCart: (Product) -> Unit
) {
    val context = LocalContext.current

    // Buscamos el producto según el ID recibido (usamos la misma lista base por ahora)
    val sampleProducts = listOf(
        Product(1, "Casco Deportivo AGV K1S", 250000.0, "Casco aerodinámico certificado...", R.drawable.casco), // Reemplaza por tu recurso
        Product(2, "Chaqueta de Cuero Dainese", 450000.0, "Chaqueta de alta resistencia...", R.drawable.chaqueta),
        Product(3, "Guantes Dainese Carbon Long", 135000.0, "Guantes deportivos largos...", R.drawable.guantes),
        Product(4, "Botas de Deportivas Dainese Toque 3", 300000.0, "Calzado deportivo con refuerzo...", R.drawable.botas)
    )

    val product = sampleProducts.find { it.id == productId } ?: sampleProducts.first()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Producto") },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen ampliada del producto (Placeholder visual)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Image(
                    painter = painterResource(id = product.imagenResId),
                    contentDescription = product.titulo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop // Llena el espacio de forma estética y proporcional
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título
            Text(
                text = product.titulo,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Precio
            Text(
                text = "$${product.precio}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Descripción completa
            Text(
                text = product.descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botón "Agregar al carrito" con retroalimentación háptica (vibración requerida por pauta)
            Button(
                onClick = {
                    triggerHapticFeedback(context)
                    onAddToCart(product)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Agregar al Carrito", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

// Función auxiliar para activar la vibración nativa del dispositivo
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