package com.example.appmovil.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.appmovil.data.local.protocolo.ProtocoloEntity
import com.example.appmovil.data.local.user.UserEntity
import com.example.appmovil.model.EquipoElectrico
import com.example.appmovil.ui.screen.EquipoDetailScreen
import com.example.appmovil.ui.screen.HomeScreen
import com.example.appmovil.ui.screen.ProfileScreen
import com.example.appmovil.ui.screen.ProtocolosScreen
import com.example.appmovil.ui.screen.LoginScreenVm
import com.example.appmovil.ui.screen.RegisterScreenVm
import com.example.appmovil.viewmodel.AuthViewModel
import com.example.appmovil.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest

// Navegación por eventos (Ej.3): cada pantalla trae su propia TopBar.
// El Home es el único con menú (tres puntitos): tema, sincronizar, perfil y salir.
@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    loggedUser: UserEntity?,
    protocolos: List<ProtocoloEntity>,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    onLoginOk: (String) -> Unit,
    onUpdateProfile: (String, String) -> Unit,
    onAddProtocolo: (EquipoElectrico, String, String) -> Unit,
    onToggleProtocolo: (ProtocoloEntity) -> Unit,
    onDeleteProtocolo: (Long) -> Unit,
    onSincronizar: (List<ProtocoloEntity>) -> Unit
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
    val goProtocolos: () -> Unit = { mainViewModel.navigateTo(Screen.Protocolos) }
    val goPerfil: () -> Unit = { mainViewModel.navigateTo(Screen.Perfil) }
    val goDetalle: (Int) -> Unit = { id -> mainViewModel.navigateTo(Screen.Detalle(id)) }
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
                    HomeScreen(
                        onNavigateToProtocolos = goProtocolos,
                        onNavigateToProfile = goPerfil,
                        onNavigateToDetail = goDetalle,
                        onToggleTheme = onToggleTheme,
                        onLogout = { onLogout(); goLogin() },
                        isDarkTheme = isDarkTheme
                    )
                }
                composable(
                    route = Route.Detalle.path,
                    arguments = listOf(navArgument(Route.Detalle.ARG) { type = NavType.IntType })
                ) { backStackEntry ->
                    val equipoId = backStackEntry.arguments?.getInt(Route.Detalle.ARG) ?: 1
                    EquipoDetailScreen(
                        equipoId = equipoId,
                        onBackClick = goBack,
                        onHomeClick = goHome,
                        onNavigateToProtocolos = goProtocolos,
                        onAddProtocol = onAddProtocolo
                    )
                }
                composable(Route.Protocolos.path) {
                    ProtocolosScreen(
                        cartItems = protocolos,
                        onToggleSelection = onToggleProtocolo,
                        onDeleteItem = onDeleteProtocolo,
                        onCheckout = { seleccionados -> onSincronizar(seleccionados) },
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
