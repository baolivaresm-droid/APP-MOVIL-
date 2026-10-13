package com.example.appmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.appmovil.data.local.database.AppDatabase
import com.example.appmovil.data.local.storage.UserPreferences
import com.example.appmovil.data.local.user.UserEntity
import com.example.appmovil.data.repository.ProtocoloRepository
import com.example.appmovil.data.repository.UserRepository
import com.example.appmovil.model.EquipoElectrico
import com.example.appmovil.navigation.AppNavGraph
import com.example.appmovil.viewmodel.AuthViewModel
import com.example.appmovil.viewmodel.AuthViewModelFactory
import com.example.appmovil.viewmodel.EstadoViewModel
import com.example.appmovil.viewmodel.MainViewModel
import com.example.appmovil.viewmodel.ProtocoloViewModel
import com.example.appmovil.viewmodel.ProtocoloViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot()
        }
    }
}

// Patrón Ej.8 MainActivity AppRoot: Composition Root con Room + repositorios + VM inyectado.
@Composable
fun AppRoot() {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    val db = remember { AppDatabase.getInstance(context) }
    val userRepository = remember { UserRepository(db.userDao()) }
    val protocoloRepository = remember { ProtocoloRepository(db.protocoloDao()) }
    val userPrefs = remember { UserPreferences(context) }

    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(userRepository))
    val mainViewModel: MainViewModel = viewModel()
    // Ej.5: el tema claro/oscuro vive en EstadoViewModel + DataStore.
    val estadoViewModel: EstadoViewModel = viewModel()
    // Ej.7: los protocolos viven en ProtocoloViewModel + StateFlow.
    val protocoloViewModel: ProtocoloViewModel = viewModel(
        factory = ProtocoloViewModelFactory(protocoloRepository)
    )

    var loggedUser by remember { mutableStateOf<UserEntity?>(null) }
    val protocolos by protocoloViewModel.protocolos.collectAsState()
    val temaActivo by estadoViewModel.activo.collectAsState()
    val isDarkTheme = temaActivo ?: false

    val navController = rememberNavController()

    AppThemeWrapper(darkTheme = isDarkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppNavGraph(
                navController = navController,
                authViewModel = authViewModel,
                mainViewModel = mainViewModel,
                loggedUser = loggedUser,
                protocolos = protocolos,
                isDarkTheme = isDarkTheme,
                onToggleTheme = { estadoViewModel.alternarEstado() },
                onLogout = {
                    loggedUser = null
                    scope.launch { userPrefs.setLoggedIn(false) }
                },
                onLoginOk = { email ->
                    scope.launch {
                        loggedUser = userRepository.getUserByEmail(email)
                    }
                },
                onUpdateProfile = { nuevaDireccion, nuevaFotoUri ->
                    scope.launch {
                        loggedUser?.let { user ->
                            val updated = user.copy(direccion = nuevaDireccion, fotoUri = nuevaFotoUri)
                            userRepository.updateUser(updated)
                            loggedUser = updated
                        }
                    }
                },
                onAddProtocolo = { equipo: EquipoElectrico, valores: String, fechaHora: String ->
                    protocoloViewModel.guardarProtocolo(equipo, valores, fechaHora)
                },
                onToggleProtocolo = { item ->
                    protocoloViewModel.alternarSeleccion(item)
                },
                onDeleteProtocolo = { id ->
                    protocoloViewModel.eliminarProtocolo(id)
                },
                onSincronizar = { seleccionados ->
                    protocoloViewModel.sincronizarSeleccionados(seleccionados)
                }
            )
        }
    }
}

@Composable
fun AppThemeWrapper(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        content = content
    )
}
