package com.example.appmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appmovil.models.AppDatabase
import com.example.appmovil.models.CartItemEntity
import com.example.appmovil.models.UserEntity
import com.example.appmovil.repository.UserRepository
import com.example.appmovil.ui.*
import com.example.appmovil.viewmodel.LoginViewModel
import com.example.appmovil.viewmodel.RegisterViewModel
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val userDao = database.appDao()
        val userRepository = UserRepository(userDao)

        val loginViewModel = LoginViewModel(userRepository)
        val registerViewModel = RegisterViewModel(userRepository)

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var loggedUser by remember { mutableStateOf<UserEntity?>(null) }
            val coroutineScope = rememberCoroutineScope()

            val cartItemsFlow = userDao.getCartItems().collectAsState(initial = emptyList())

            AppThemeWrapper(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {

                    // Pantalla 1: Login
                    composable("login") {
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {
                                coroutineScope.launch {
                                    loggedUser = userRepository.getUserByEmail(loginViewModel.email.trim())
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate("register")
                            }
                        )
                    }

                    // Pantalla 2: Registro
                    composable("register") {
                        RegisterScreen(
                            viewModel = registerViewModel,
                            onRegisterSuccess = {
                                navController.popBackStack()
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // Pantalla 3: Home (Catálogo de Equipos)
                    composable("home") {
                        HomeScreen(
                            onNavigateToCart = { navController.navigate("cart") },
                            onNavigateToProfile = { navController.navigate("profile") },
                            onNavigateToDetail = { productId ->
                                navController.navigate("detail/$productId")
                            },
                            onToggleTheme = { isDarkTheme = !isDarkTheme },
                            onLogout = {
                                loggedUser = null
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    // Pantalla 4: Detalle del Equipo y Registro de Mediciones en Terreno
                    composable(
                        route = "detail/{productId}",
                        arguments = listOf(navArgument("productId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val productId = backStackEntry.arguments?.getInt("productId") ?: 1
                        ProductDetailScreen(
                            productId = productId,
                            onBackClick = { navController.popBackStack() },
                            onHomeClick = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigateToCart = {
                                // Abre la pantalla de protocolos directamente desde el icono superior
                                navController.navigate("cart")
                            },
                            onAddProtocol = { product, valoresMedicion, fechaHora ->
                                coroutineScope.launch {
                                    val cartItem = CartItemEntity(
                                        productId = product.id,
                                        titulo = product.titulo,
                                        codigoEquipo = product.codigoEquipo,
                                        ubicacionCliente = product.ubicacionCliente,
                                        valoresMedicion = valoresMedicion,
                                        fechaHora = fechaHora,
                                        imagenResId = product.imagenResId,
                                        cantidad = 1,
                                        isSelected = true
                                    )
                                    userDao.insertCartItem(cartItem)
                                }
                            }
                        )
                    }

                    // Pantalla 5: Listado de Protocolos Registrados
                    composable("cart") {
                        CartScreen(
                            cartItems = cartItemsFlow.value,
                            onToggleSelection = { item ->
                                coroutineScope.launch {
                                    userDao.updateCartItem(item.copy(isSelected = !item.isSelected))
                                }
                            },
                            onDeleteItem = { itemId ->
                                coroutineScope.launch {
                                    userDao.deleteCartItem(itemId)
                                }
                            },
                            onCheckout = {
                                coroutineScope.launch {
                                    userDao.clearCart()
                                    navController.popBackStack()
                                }
                            },
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onHomeClick = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Pantalla 6: Mi Perfil (Técnico)
                    composable("profile") {
                        loggedUser?.let { user ->
                            ProfileScreen(
                                user = user,
                                onUpdateProfile = { nuevaDireccion, nuevaFotoUri ->
                                    coroutineScope.launch {
                                        val updatedUser = user.copy(direccion = nuevaDireccion, fotoUri = nuevaFotoUri)
                                        userRepository.updateUser(updatedUser)
                                        loggedUser = updatedUser
                                        navController.popBackStack()
                                    }
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
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