package com.example.appmovil.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmovil.data.local.protocolo.ProtocoloEntity

// Listado de protocolos en terreno.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtocolosScreen(
    cartItems: List<ProtocoloEntity>,
    onToggleSelection: (ProtocoloEntity) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onCheckout: (List<ProtocoloEntity>) -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val totalProtocolos = cartItems.count { it.isSelected }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Ej.5: animar color del botón sincronizar según selección
    val syncColor by animateColorAsState(
        targetValue = if (totalProtocolos > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 500),
        label = "syncColor"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Protocolos de Prueba en Terreno") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver al producto")
                    }
                },
                actions = {
                    IconButton(onClick = onHomeClick) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Ir al Inicio")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = cartItems.isNotEmpty()) {
                Surface(tonalElevation = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Protocolos seleccionados para envío:")
                            Text(text = "$totalProtocolos", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val seleccionados = cartItems.filter { it.isSelected }
                                if (seleccionados.isEmpty()) return@Button
                                onCheckout(seleccionados)
                                val mensaje = if (seleccionados.size == 1) "Protocolo sincronizado"
                                else "${seleccionados.size} protocolos sincronizados"
                                scope.launch { snackbarHostState.showSnackbar(mensaje) }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = cartItems.any { it.isSelected },
                            colors = ButtonDefaults.buttonColors(containerColor = syncColor)
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Sincronizar Protocolos (Offline)", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay protocolos de prueba registrados en esta visita",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems, key = { it.id }) { item ->
                    ProtocoloCard(
                        item = item,
                        onToggle = { onToggleSelection(item) },
                        onDelete = { onDeleteItem(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProtocoloCard(
    item: ProtocoloEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = item.isSelected, onCheckedChange = { onToggle() })
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Cód: ${item.codigoEquipo} | ${item.ubicacionCliente}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Medición: ${item.valoresMedicion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Registrado: ${item.fechaHora}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Compatibilidad con nombres anteriores de la plantilla.
@Composable
fun CartScreen(
    cartItems: List<ProtocoloEntity>,
    onToggleSelection: (ProtocoloEntity) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onCheckout: (List<ProtocoloEntity>) -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit
) = ProtocolosScreen(cartItems, onToggleSelection, onDeleteItem, onCheckout, onBackClick, onHomeClick)

@Composable
fun CartItemCard(item: ProtocoloEntity, onToggle: () -> Unit, onDelete: () -> Unit) =
    ProtocoloCard(item = item, onToggle = onToggle, onDelete = onDelete)
