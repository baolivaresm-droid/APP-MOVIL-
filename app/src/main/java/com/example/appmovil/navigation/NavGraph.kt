package com.example.appmovil.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.appmovil.data.local.user.UserEntity
import com.example.appmovil.ui.screen.EmpresaDetailScreen
import com.example.appmovil.ui.screen.HomeScreen
import com.example.appmovil.ui.screen.InformeScreen
import com.example.appmovil.ui.screen.MedicionFormScreen
import com.example.appmovil.ui.screen.ProfileScreen
import com.example.appmovil.ui.screen.LoginScreenVm
import com.example.appmovil.ui.screen.RegisterScreenVm
import com.example.appmovil.viewmodel.AuthViewModel
import com.example.appmovil.viewmodel.EmpresaViewModel
import com.example.appmovil.viewmodel.MainViewModel
import com.example.appmovil.viewmodel.MaquinaViewModel
import com.example.appmovil.viewmodel.MedicionViewModel
import kotlinx.coroutines.flow.collectLatest

// Navegación por eventos (Ej.3): cada pantalla trae su propia TopBar.
// CasoA 7 pantallas: Login, Registro, Home empresas, Detalle empresa,
// Formulario medición, Informe PDF, Perfil.
@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    empresaViewModel: EmpresaViewModel,
    maquinaViewModel: MaquinaViewModel,
    medicionViewModel: MedicionViewModel,
    loggedUser: UserEntity?,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    onLoginOk: (String) -> Unit,
    onUpdateProfile: (String, String) -> Unit
) {
    // Ej.3: un solo colector ejecuta los eventos sobre el navController.
    LaunchedEffect(mainViewModel) {
        mainViewModel.navigationEvents.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateTo -> {
                    navController.navigate(event.screen.route) {
                        launchSingleTop = event.singleTop
                        if (event.clearBackStack) {
                            popUpTo(0) { inclusive = true }
                        } else {
                            event.popUpTo?.let { popUpTo(it.route) { inclusive = event.inclusive } }
                        }
                    }
                }
                NavigationEvent.PopBackStack -> navController.popBackStack()
                NavigationEvent.NavigateUp -> navController.navigateUp()
            }
        }
    }

    val goHome: () -> Unit = { mainViewModel.navigateTo(Screen.Home) }
    val goLogin: () -> Unit = { mainViewModel.navigateTo(Screen.Login, clearBackStack = true) }
    val goRegister: () -> Unit = { mainViewModel.navigateTo(Screen.Register) }
    val goPerfil: () -> Unit = { mainViewModel.navigateTo(Screen.Perfil) }
    val goEmpresa: (Long) -> Unit = { id -> mainViewModel.navigateTo(Screen.EmpresaDetalle(id)) }
    val goMedicion: (Long) -> Unit = { id -> mainViewModel.navigateTo(Screen.MedicionForm(id)) }
    val goInforme: (Long) -> Unit = { id -> mainViewModel.navigateTo(Screen.Informe(id)) }
    val goBack: () -> Unit = { mainViewModel.popBackStack() }

    NavHost(
        navController = navController,
        startDestination = Route.Login.path,
        modifier = Modifier
    ) {
        composable(Route.Login.path) {
            LoginScreenVm(
                vm = authViewModel,
                onLoginOkNavigateHome = {
                    onLoginOk(authViewModel.login.value.email.trim())
                    goHome()
                },
                onGoRegister = goRegister
            )
        }
        composable(Route.Register.path) {
            RegisterScreenVm(
                vm = authViewModel,
                onRegisteredNavigateLogin = goBack,
                onGoLogin = goBack
            )
        }
        composable(Route.Home.path) {
            val empresas = empresaViewModel.empresas.collectAsStateWithLifecycle().value
            HomeScreen(
                empresas = empresas,
                onAddEmpresa = { nombre, zona -> empresaViewModel.guardarEmpresa(nombre, zona) },
                onDeleteEmpresa = { empresaViewModel.eliminarEmpresa(it) },
                onNavigateToEmpresa = goEmpresa,
                onNavigateToProfile = goPerfil,
                onToggleTheme = onToggleTheme,
                onLogout = { onLogout(); goLogin() },
                isDarkTheme = isDarkTheme
            )
        }
        composable(
            route = Route.EmpresaDetalle.path,
            arguments = listOf(navArgument(Route.EmpresaDetalle.ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val empresaId = backStackEntry.arguments?.getLong(Route.EmpresaDetalle.ARG) ?: 0L
            val empresas = empresaViewModel.empresas.collectAsStateWithLifecycle().value
            val empresa = empresas.find { it.id == empresaId }
            EmpresaDetailScreen(
                empresaId = empresaId,
                empresaNombre = empresa?.nombre.orEmpty(),
                empresaZona = empresa?.zona.orEmpty(),
                maquinaViewModel = maquinaViewModel,
                onBackClick = goBack,
                onHomeClick = goHome,
                onNavigateToMedicion = goMedicion
            )
        }
        composable(
            route = Route.MedicionForm.path,
            arguments = listOf(navArgument(Route.MedicionForm.ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val maquinaId = backStackEntry.arguments?.getLong(Route.MedicionForm.ARG) ?: 0L
            MedicionFormScreen(
                maquinaId = maquinaId,
                maquinaViewModel = maquinaViewModel,
                medicionViewModel = medicionViewModel,
                onBackClick = goBack,
                onHomeClick = goHome,
                onMedicionGuardada = goInforme
            )
        }
        composable(
            route = Route.Informe.path,
            arguments = listOf(navArgument(Route.Informe.ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val medicionId = backStackEntry.arguments?.getLong(Route.Informe.ARG) ?: 0L
            InformeScreen(
                medicionId = medicionId,
                medicionViewModel = medicionViewModel,
                maquinaViewModel = maquinaViewModel,
                onBackClick = goBack,
                onHomeClick = goHome
            )
        }
        composable(Route.Perfil.path) {
            loggedUser?.let { user ->
                ProfileScreen(
                    user = user,
                    onUpdateProfile = { dir, foto ->
                        onUpdateProfile(dir, foto)
                        goBack()
                    },
                    onBackClick = goBack
                )
            }
        }
    }
}
