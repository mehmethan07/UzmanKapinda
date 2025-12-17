package com.example.uzmankapinda

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.uzmankapinda.ui.theme.UzmanKapindaTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        setContent {
            UzmanKapindaTheme { Surface(modifier = Modifier.fillMaxSize()) { AppNavHost() } }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val cartItems = remember { mutableStateListOf<CartItem>().apply { addAll(CartManager.loadCart(context)) } }

    var isUsd by remember { mutableStateOf(false) }
    var usdRate by remember { mutableStateOf<Double?>(null) }

    fun saveCartLocally() {
        CartManager.saveCart(context, cartItems)
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = ApiManager.exchangeService.getRates()
                usdRate = response.rates["USD"]
            } catch (e: Exception) {
                // Kur çekme hatası (production'da loglanmalı, kullanıcıya gösterilmemeli)
            }
        }
    }

    fun addToCart(service: ServiceModel) {
        val existing = cartItems.find { it.service.id == service.id }

        if (existing != null) {
            if (existing.quantity < service.stock) {
                val index = cartItems.indexOf(existing)
                cartItems[index] = existing.copy(quantity = existing.quantity + 1)
                saveCartLocally()
                Toast.makeText(context, "Sepet güncellendi", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Stokta sadece ${service.stock} adet var!", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (service.stock > 0) {
                cartItems.add(CartItem(service = service, quantity = 1))
                saveCartLocally()
                Toast.makeText(context, "Sepete eklendi", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Ürün stokta yok!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginAsUser = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                onLoginAsAdmin = { navController.navigate("adminHome") { popUpTo("login") { inclusive = true } } },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgotPassword") }
            )
        }
        composable("forgotPassword") { ForgotPasswordScreen(onBack = { navController.popBackStack() }) }
        composable("register") { RegisterScreen(onBack = { navController.popBackStack() }, onNavigateToLogin = { navController.popBackStack() }) }

        composable("home") {
            HomeScreen(
                onAddToCart = { service -> addToCart(service) },
                onGoToCart = { navController.navigate("cart") },
                cartItemCount = cartItems.sumOf { it.quantity },
                onServiceClick = { service -> navController.navigate("serviceDetail/${service.id}") },
                onProfileClick = { navController.navigate("profile") },
                isUsd = isUsd,
                onCurrencyToggle = { isUsd = !isUsd },
                usdRate = usdRate
            )
        }

        composable("cart") {
            CartScreen(
                cartItems = cartItems,
                onIncrease = { item ->
                    val index = cartItems.indexOf(item)
                    if (index != -1 && item.quantity < item.service.stock) {
                        cartItems[index] = item.copy(quantity = item.quantity + 1)
                        saveCartLocally()
                    } else {
                        Toast.makeText(context, "Maksimum stok", Toast.LENGTH_SHORT).show()
                    }
                },
                onDecrease = { item ->
                    val index = cartItems.indexOf(item)
                    if (index != -1) {
                        val newQty = item.quantity - 1
                        if (newQty <= 0) {
                            cartItems.removeAt(index)
                        } else {
                            cartItems[index] = item.copy(quantity = newQty)
                        }
                        saveCartLocally()
                    }
                },
                onBack = { navController.popBackStack() },
                onCheckout = { navController.navigate("checkout") },
                isUsd = isUsd,
                usdRate = usdRate
            )
        }

        composable("checkout") {
            CheckoutScreen(
                cartItems = cartItems,
                onBack = { navController.popBackStack() },
                onOrderSuccess = {
                    cartItems.clear()
                    CartManager.clearCart(context)
                    navController.navigate("home") { popUpTo("home") { inclusive = false } }
                },
                isUsd = isUsd,
                usdRate = usdRate
            )
        }

        composable("serviceDetail/{serviceId}") { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            ServiceDetailScreen(
                serviceId = serviceId,
                onBack = { navController.popBackStack() },
                onAddToCart = { service -> addToCart(service) },
                isUsd = isUsd,
                usdRate = usdRate
            )
        }

        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogoutClick = { AuthManager.logout(); navController.navigate("login") { popUpTo(0) { inclusive = true } } },
                onViewOrdersClick = { navController.navigate("userOrders") }
            )
        }
        composable("userOrders") { UserOrdersScreen(onBack = { navController.popBackStack() }) }

        // ADMIN NAVIGASYONU
        composable("adminHome") {
            AdminHomeScreen(
                onNavigateToAddService = { navController.navigate("adminAddService") },
                onLogout = { AuthManager.logout(); navController.navigate("login") { popUpTo(0) { inclusive = true } } },
                onNavigateToEditService = { serviceId -> navController.navigate("adminEditService/$serviceId") },
                onNavigateToOrders = { navController.navigate("adminOrders") },
                onNavigateToUsers = { navController.navigate("adminUsers") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToQrScanner = { navController.navigate("adminQrScanner") }
            )
        }
        composable("adminAddService") {
            AdminAddServiceScreen(
                onServiceAdded = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable("adminEditService/{serviceId}") { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            AdminEditServiceScreen(
                serviceId = serviceId,
                onServiceUpdated = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable("adminOrders") { AdminOrdersScreen(onBack = { navController.popBackStack() }) }
        composable("adminUsers") { AdminUsersScreen(onBack = { navController.popBackStack() }) }

        composable("adminQrScanner") {
            AdminQrScannerScreen(onBack = { navController.popBackStack() })
        }
    }
}