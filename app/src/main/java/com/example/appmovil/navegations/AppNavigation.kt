package com.example.appmovil.navegations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appmovil.repository.models.ROL_ADMIN
import com.example.appmovil.view.ui.AdminEmpresaScreen
import com.example.appmovil.view.ui.AdminScreen
import com.example.appmovil.view.ui.HomeScreen
import com.example.appmovil.view.ui.InformeScreen
import com.example.appmovil.view.ui.LoginScreen
import com.example.appmovil.view.ui.MedicionFormScreen
import com.example.appmovil.view.ui.ProfileScreen
import com.example.appmovil.view.ui.RegistroScreen
import com.example.appmovil.view.ui.SucursalDetailScreen
import com.example.appmovil.view.ui.TecnicoEmpresaScreen
import com.example.appmovil.view.utils.navEnterTransition
import com.example.appmovil.view.utils.navExitTransition
import com.example.appmovil.view.utils.navPopEnterTransition
import com.example.appmovil.view.utils.navPopExitTransition
import com.example.appmovil.viewmodels.AdminViewModel
import com.example.appmovil.viewmodels.AsignacionViewModel
import com.example.appmovil.viewmodels.EmpresaViewModel
import com.example.appmovil.viewmodels.InformeViewModel
import com.example.appmovil.viewmodels.LoginViewModel
import com.example.appmovil.viewmodels.MaquinaViewModel
import com.example.appmovil.viewmodels.MedicionViewModel
import com.example.appmovil.viewmodels.RegistroViewModel
import com.example.appmovil.viewmodels.SucursalViewModel
import com.example.appmovil.viewmodels.TemaViewModel
import kotlinx.coroutines.launch

// Rutas de navegación de la app
object Routes {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val HOME = "home"
    const val ADMIN = "admin"
    const val PERFIL = "perfil"
    const val EMPRESA = "empresa/{empresaId}"
    const val ADMIN_EMPRESA = "adminEmpresa/{empresaId}"
    const val SUCURSAL = "sucursal/{sucursalId}"
    const val MEDICION = "medicion/{maquinaId}"
    const val INFORME = "informe/{medicionId}"
}

data class ItemMenu(val titulo: String, val icono: ImageVector, val ruta: String)

private fun tituloDeRuta(ruta: String?): String = when {
    ruta == Routes.HOME -> "Induztek · Inicio"
    ruta == Routes.ADMIN -> "Panel de Administración"
    ruta == Routes.PERFIL -> "Mi Perfil"
    ruta == Routes.REGISTRO -> "Registro de Técnico"
    ruta?.startsWith("empresa/") == true -> "Empresa"
    ruta?.startsWith("adminEmpresa/") == true -> "Empresa"
    ruta?.startsWith("sucursal/") == true -> "Sucursal"
    ruta?.startsWith("medicion/") == true -> "Medición en terreno"
    ruta?.startsWith("informe/") == true -> "Informe del protocolo"
    else -> "Induztek"
}

// Gráfico de navegación principal: drawer + pantallas.
// El rol del usuario decide el destino del login y las opciones del menú.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(temaViewModel: TemaViewModel) {
    val navController = rememberNavController()

    // ViewModels de toda la app (AndroidViewModel + Room, sin Factory)
    val loginViewModel: LoginViewModel = viewModel()
    val registroViewModel: RegistroViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()
    val asignacionViewModel: AsignacionViewModel = viewModel()
    val empresaViewModel: EmpresaViewModel = viewModel()
    val sucursalViewModel: SucursalViewModel = viewModel()
    val maquinaViewModel: MaquinaViewModel = viewModel()
    val medicionViewModel: MedicionViewModel = viewModel()
    val informeViewModel: InformeViewModel = viewModel()

    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val modoOscuro by temaViewModel.activo.collectAsStateWithLifecycle()
    val usuario = loginState.loggedUser

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val enLogin = rutaActual == Routes.LOGIN || rutaActual == Routes.REGISTRO
    val esAdmin = usuario?.rol == ROL_ADMIN
    val raiz = if (esAdmin) Routes.ADMIN else Routes.HOME

    // Menú lateral filtrado por rol
    val items = if (esAdmin) {
        listOf(
            ItemMenu("Panel de Administración", Icons.Filled.BusinessCenter, Routes.ADMIN),
            ItemMenu("Mi Perfil", Icons.Filled.Person, Routes.PERFIL)
        )
    } else {
        listOf(
            ItemMenu("Inicio", Icons.Filled.Home, Routes.HOME),
            ItemMenu("Mi Perfil", Icons.Filled.Person, Routes.PERFIL)
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = usuario != null && !enLogin,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Induztek", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = usuario?.nombre ?: "Sin sesión",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                items.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.titulo) },
                        selected = rutaActual == item.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.ruta) {
                                popUpTo(raiz) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icono, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                // Cambio de tema claro/oscuro persistido en DataStore
                NavigationDrawerItem(
                    label = { Text(if (modoOscuro == true) "Tema claro" else "Tema oscuro") },
                    selected = false,
                    onClick = { temaViewModel.alternarEstado() },
                    icon = {
                        Icon(
                            imageVector = if (modoOscuro == true) Icons.Filled.LightMode
                            else Icons.Filled.DarkMode,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        loginViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            // Limpia todo el back stack al cerrar sesión
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Logout, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!enLogin) {
                    TopAppBar(
                        title = { Text(tituloDeRuta(rutaActual)) },
                        navigationIcon = {
                            if (rutaActual == raiz) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                                }
                            } else {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver"
                                    )
                                }
                            }
                        },
                        actions = {
                            if (rutaActual != raiz) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        ) { padding ->
            // ANIMACIÓN 3 (view/utils/Animations.kt): transiciones entre pantallas
            NavHost(
                navController = navController,
                startDestination = Routes.LOGIN,
                modifier = Modifier.padding(padding),
                enterTransition = navEnterTransition,
                exitTransition = navExitTransition,
                popEnterTransition = navPopEnterTransition,
                popExitTransition = navPopExitTransition
            ) {
                composable(Routes.LOGIN) {
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = {
                            // Según el rol: ADMIN al panel, TECNICO al home
                            val destino =
                                if (loginViewModel.uiState.value.loggedUser?.rol == ROL_ADMIN)
                                    Routes.ADMIN else Routes.HOME
                            navController.navigate(destino) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        },
                        onGoRegister = { navController.navigate(Routes.REGISTRO) }
                    )
                }

                composable(Routes.REGISTRO) {
                    RegistroScreen(
                        viewModel = registroViewModel,
                        onRegisteredNavigateLogin = { navController.popBackStack() },
                        onGoLogin = { navController.popBackStack() }
                    )
                }

                composable(Routes.HOME) {
                    HomeScreen(
                        viewModel = asignacionViewModel,
                        usuario = usuario,
                        onNavigateToEmpresa = { navController.navigate("empresa/$it") }
                    )
                }

                composable(Routes.ADMIN) {
                    AdminScreen(
                        adminViewModel = adminViewModel,
                        informeViewModel = informeViewModel,
                        onNavigateToEmpresa = { navController.navigate("adminEmpresa/$it") }
                    )
                }

                composable(Routes.PERFIL) {
                    ProfileScreen(
                        usuario = usuario,
                        onUpdateProfile = { direccion, fotoUri ->
                            loginViewModel.actualizarPerfil(direccion, fotoUri)
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = Routes.EMPRESA,
                    arguments = listOf(navArgument("empresaId") { type = NavType.LongType })
                ) { entry ->
                    TecnicoEmpresaScreen(
                        viewModel = asignacionViewModel,
                        usuario = usuario,
                        empresaId = entry.arguments?.getLong("empresaId") ?: 0L,
                        onNavigateToSucursal = { navController.navigate("sucursal/$it") }
                    )
                }

                composable(
                    route = Routes.ADMIN_EMPRESA,
                    arguments = listOf(navArgument("empresaId") { type = NavType.LongType })
                ) { entry ->
                    AdminEmpresaScreen(
                        empresaId = entry.arguments?.getLong("empresaId") ?: 0L,
                        adminViewModel = adminViewModel,
                        onAddSucursal = { empresaId, nombre, direccion, comuna, telefono ->
                            adminViewModel.guardarSucursal(
                                empresaId, nombre, direccion, comuna, telefono
                            )
                        }
                    )
                }

                composable(
                    route = Routes.SUCURSAL,
                    arguments = listOf(navArgument("sucursalId") { type = NavType.LongType })
                ) { entry ->
                    SucursalDetailScreen(
                        sucursalId = entry.arguments?.getLong("sucursalId") ?: 0L,
                        usuarioId = usuario?.id ?: 0L,
                        sucursalViewModel = sucursalViewModel,
                        empresaViewModel = empresaViewModel,
                        maquinaViewModel = maquinaViewModel,
                        onNavigateToMedicion = { navController.navigate("medicion/$it") }
                    )
                }

                composable(
                    route = Routes.MEDICION,
                    arguments = listOf(navArgument("maquinaId") { type = NavType.LongType })
                ) { entry ->
                    MedicionFormScreen(
                        maquinaId = entry.arguments?.getLong("maquinaId") ?: 0L,
                        maquinaViewModel = maquinaViewModel,
                        medicionViewModel = medicionViewModel,
                        onBackClick = { navController.popBackStack() },
                        onMedicionGuardada = { navController.navigate("informe/$it") },
                        onNavigateToInforme = { navController.navigate("informe/$it") }
                    )
                }

                composable(
                    route = Routes.INFORME,
                    arguments = listOf(navArgument("medicionId") { type = NavType.LongType })
                ) { entry ->
                    InformeScreen(
                        medicionId = entry.arguments?.getLong("medicionId") ?: 0L,
                        medicionViewModel = medicionViewModel,
                        maquinaViewModel = maquinaViewModel,
                        sucursalViewModel = sucursalViewModel,
                        empresaViewModel = empresaViewModel,
                        informeViewModel = informeViewModel,
                        tecnico = usuario,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
