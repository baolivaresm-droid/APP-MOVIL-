package com.example.appmovil.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmovil.R
import com.example.appmovil.models.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit // <--- 1. Nuevo parámetro para cerrar sesión
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val sampleProducts = listOf(
        Product(
            1,
            "Transformador de Potencia 220kV",
            "TR-220-01",
            "Subestación El Salto",
            "Aislamiento anterior: 2.5 GΩ",
            "Pruebas de resistencia de aislamiento y tangente delta.",
            R.drawable.transformador
        ),
        Product(
            2,
            "Relé de Protección Sepam",
            "REL-SEP-04",
            "Planta Maipú",
            "Umbral anterior: 5.2 A",
            "Prueba de inyección de corrientes secundarias.",
            R.drawable.rele
        ),
        Product(
            3,
            "Celda de Media Tensión 12kV",
            "CEL-12V-09",
            "Distribución Quilicura",
            "Resistencia anterior: 45 µΩ",
            "Inspección de sistemas de enclavamiento.",
            R.drawable.celda
        ),
        Product(
            4,
            "Banco de Baterías Subestación",
            "BAT-SUB-02",
            "Subestación Lo Espejo",
            "Voltaje anterior: 2.15 V/celda",
            "Prueba de descarga y resistencia interna.",
            R.drawable.baterias
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equipos Eléctricos - Induztek") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menú")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mi Perfil (Técnico)") },
                            onClick = {
                                menuExpanded = false
                                onNavigateToProfile()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Protocolos Registrados") },
                            onClick = {
                                menuExpanded = false
                                onNavigateToCart()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cambiar Tema (Claro/Oscuro)") },
                            onClick = {
                                menuExpanded = false
                                onToggleTheme()
                            }
                        )
                        HorizontalDivider() // Línea divisoria estética
                        DropdownMenuItem(
                            text = { Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onLogout() // <--- Ejecuta la acción de salida
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sampleProducts) { product ->
                ProductCard(product = product, onClick = { onNavigateToDetail(product.id) })
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Image(
                    painter = painterResource(id = product.imagenResId),
                    contentDescription = product.titulo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Cód: ${product.codigoEquipo}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = product.ubicacionCliente,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}