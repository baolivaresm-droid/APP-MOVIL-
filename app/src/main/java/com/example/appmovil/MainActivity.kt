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
import com.example.appmovil.models.Product
import com.example.appmovil.models.UserEntity
import com.example.appmovil.repository.UserRepository
import com.example.appmovil.ui.*
import com.example.appmovil.viewmodel.LoginViewModel
import com.example.appmovil.viewmodel.RegisterViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializamos la base de datos de Room y el repositorio
        val database = AppDatabase.getDatabase(applicationContext)
        val userDao = database.appDao()
        val userRepository = UserRepository(userDao)

        // 2. Instanciamos los ViewModels
        val loginViewModel = LoginViewModel(userRepository)
        val registerViewModel = RegisterViewModel(userRepository)

        setContent {
            // Estado global simple para alternar el tema claro/oscuro requerido por pauta
            var isDarkTheme by remember { mutableStateOf(false) }

            // Estado de la sesión del usuario actual y carrito
            var loggedUser by remember { mutableStateOf<UserEntity?>(null) }
            val coroutineScope = rememberCoroutineScope()

            // Observamos los elementos del carrito desde Room en tiempo real
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
                                    // Obtenemos los datos del usuario logueado para pasarlos al perfil
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

                    // Pantalla 3: Home (Catálogo)
                    composable("home") {
                        HomeScreen(
                            onNavigateToCart = { navController.navigate("cart") },
                            onNavigateToProfile = { navController.navigate("profile") },
                            onNavigateToDetail = { productId ->
                                navController.navigate("detail/$productId")
                            },
                            onToggleTheme = { isDarkTheme = !isDarkTheme }
                        )
                    }

                    // Pantalla 4: Detalle del Producto
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
                            onAddToCart = { product ->
                                coroutineScope.launch {
                                    // Guardamos el producto en la tabla carrito de Room
                                    val cartItem = CartItemEntity(
                                        productId = product.id,
                                        titulo = product.titulo,
                                        precio = product.precio,
                                        imagenResId = product.imagenResId,
                                        cantidad = 1,
                                        isSelected = true
                                    )
                                    userDao.insertCartItem(cartItem)
                                    navController.popBackStack()
                                }
                            }
                        )
                    }

                    // Pantalla 5: Carrito de Compras
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
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    // Pantalla 6: Mi Perfil
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
    // Aplica el esquema de colores Material 3 Claro u Oscuro requerido por pauta
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        content = content
    )
}