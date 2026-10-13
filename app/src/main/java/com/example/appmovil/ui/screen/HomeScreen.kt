package com.example.appmovil.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appmovil.R
import com.example.appmovil.data.local.storage.UserPreferences
import com.example.appmovil.model.EquipoElectrico
import kotlinx.coroutines.delay

// Catálogo de equipos Induztek. Patrón Ej.1 Scaffold+TopAppBar + Ej.5 animación + Ej.8 DataStore.
val equiposInduztek = listOf(
    EquipoElectrico(
        1,
        "Transformador de Potencia 220kV",
        "TR-220-01",
        "Subestación El Salto",
        "Aislamiento anterior: 2.5 GΩ",
        "Pruebas de resistencia de aislamiento y tangente delta.",
        R.drawable.transformador
    ),
    EquipoElectrico(
        2,
        "Relé de Protección Sepam",
        "REL-SEP-04",
        "Planta Maipú",
        "Umbral anterior: 5.2 A",
        "Prueba de inyección de corrientes secundarias.",
        R.drawable.rele
    ),
    EquipoElectrico(
        3,
        "Celda de Media Tensión 12kV",
        "CEL-12V-09",
        "Distribución Quilicura",
        "Resistencia anterior: 45 µΩ",
        "Inspección de sistemas de enclavamiento.",
        R.drawable.celda
    ),
    EquipoElectrico(
        4,
        "Banco de Baterías Subestación",
        "BAT-SUB-02",
        "Subestación Lo Espejo",
        "Voltaje anterior: 2.15 V/celda",
        "Prueba de descarga y resistencia interna.",
        R.drawable.baterias
    )
)

fun buscarEquipo(equipoId: Int): EquipoElectrico =
    equiposInduztek.find { it.id == equipoId } ?: equiposInduztek.first()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProtocolos: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean = false
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showThemeMsg by remember { mutableStateOf(false) }

    // Ej.5: el mensaje de confirmación se auto-oculta a los 2 segundos.
    LaunchedEffect(showThemeMsg) {
        if (showThemeMsg) {
            delay(2000)
            showThemeMsg = false
        }
    }

    // Ej.8: estado de sesión desde DataStore para el icono Person/PersonOff
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val isLoggedIn by userPrefs.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)

    // Evaluación compacta (solo celular): grilla fija de 2 columnas.
    val columnas = 2

    // Ej.5: animar color del botón de tema según estado
    val themeColor by animateColorAsState(
        targetValue = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        animationSpec = tween(durationMillis = 500),
        label = "themeColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equipos Eléctricos - Induztek") },
                navigationIcon = {
                    Icon(
                        imageVector = if (isLoggedIn) Icons.Filled.Person else Icons.Filled.PersonOff,
                        contentDescription = if (isLoggedIn) "Sesión activa" else "Sesión inactiva",
                        tint = themeColor
                    )
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menú")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Mi Perfil (Técnico)") },
                            onClick = { menuExpanded = false; onNavigateToProfile() }
                        )
                        DropdownMenuItem(
                            text = { Text("Protocolos Registrados") },
                            onClick = { menuExpanded = false; onNavigateToProtocolos() }
                        )
                        DropdownMenuItem(
                            text = { Text("Cambiar Tema (Claro/Oscuro)") },
                            onClick = {
                                menuExpanded = false
                                onToggleTheme()
                                showThemeMsg = true
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error) },
                            onClick = { menuExpanded = false; onLogout() }
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
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Ej.5 AnimatedVisibility: retroalimentación al cambiar tema
            AnimatedVisibility(visible = showThemeMsg) {
                Text(
                    text = if (isDarkTheme) "Modo oscuro activado" else "Modo claro activado",
                    style = MaterialTheme.typography.labelLarge,
                    color = themeColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnas),
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(equiposInduztek) { equipo ->
                    EquipoCard(equipo = equipo, onClick = { onNavigateToDetail(equipo.id) })
                }
            }
        }
    }
}

@Composable
fun EquipoCard(equipo: EquipoElectrico, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Image(
                    painter = painterResource(id = equipo.imagenResId),
                    contentDescription = equipo.titulo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = equipo.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cód: ${equipo.codigoEquipo}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = equipo.ubicacionCliente,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
