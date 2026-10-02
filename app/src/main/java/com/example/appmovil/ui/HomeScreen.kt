package com.example.appmovil.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
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
    onToggleTheme: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val sampleProducts = listOf(
        Product(1, "Casco Deportivo AGV K1S", 250000.0, "Casco aerodinámico certificado con visor antivaho y ventilación avanzada para máxima seguridad en carretera y pista.", R.drawable.casco),
        Product(2, "Chaqueta de Cuero Dainese", 450000.0, "Chaqueta de alta resistencia con protecciones homologadas en hombros, codos y espalda. Ideal para cualquier clima.", R.drawable.chaqueta),
        Product(3, "Guantes Dainese Carbon Long", 135000.0, "Guantes deportivos largos con protección en nudillos de fibra de carbono y refuerzo antideslizante en la palma.", R.drawable.guantes),
        Product(4, "Botas de Deportivas Dainese Toque 3", 300000.0, "Calzado deportivo con refuerzo en talón, tobillo y protección para el cambio de marchas.", R.drawable.botas)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Productos") },
                actions = {
                    IconButton(onClick = onNavigateToCart) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Carrito")
                    }

                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menú")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mi Perfil") },
                            onClick = {
                                menuExpanded = false
                                onNavigateToProfile()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Carrito de Compras") },
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
                text = "$${product.precio}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}