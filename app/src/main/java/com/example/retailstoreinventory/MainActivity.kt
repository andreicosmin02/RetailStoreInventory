package com.example.retailstoreinventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import com.example.retailstoreinventory.data.models.Product
import com.example.retailstoreinventory.data.repository.ProductRepository
import com.example.retailstoreinventory.ui.screens.*
import com.example.retailstoreinventory.ui.theme.RetailStoreInventoryTheme
import com.example.retailstoreinventory.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

sealed class Screen {
    object Main : Screen()
    data class Details(val product: Product) : Screen()
    object Orders : Screen()
    object Providers : Screen()
    object Logs : Screen()
    object Scanner : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent {
            RetailStoreInventoryTheme(darkTheme = true) {
                val repository = FakeProductRepository() // Temporary implementation
                val viewModel = remember { ProductViewModel(repository) }

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                                Text("RetailPro", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            Spacer(Modifier.height(12.dp))
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Inventory, null) },
                                label = { Text("Inventory") },
                                selected = currentScreen is Screen.Main,
                                onClick = { currentScreen = Screen.Main; scope.launch { drawerState.close() } },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.ShoppingCart, null) },
                                label = { Text("Orders") },
                                selected = currentScreen is Screen.Orders,
                                onClick = { currentScreen = Screen.Orders; scope.launch { drawerState.close() } },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Business, null) },
                                label = { Text("Providers") },
                                selected = currentScreen is Screen.Providers,
                                onClick = { currentScreen = Screen.Providers; scope.launch { drawerState.close() } },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.History, null) },
                                label = { Text("Logs") },
                                selected = currentScreen is Screen.Logs,
                                onClick = { currentScreen = Screen.Logs; scope.launch { drawerState.close() } },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                ) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        when (val screen = currentScreen) {
                            is Screen.Main -> MainScreen(
                                products = viewModel.products,
                                onItemClick = { product -> currentScreen = Screen.Details(product) },
                                onScanClick = { currentScreen = Screen.Scanner },
                                onSearch = { query -> viewModel.searchProducts(query) }
                            )
                            is Screen.Details -> DetailsScreen(product = screen.product, onBack = { currentScreen = Screen.Main })
                            is Screen.Orders -> OrdersScreen(onBack = { currentScreen = Screen.Main })
                            is Screen.Providers -> ProvidersScreen(onBack = { currentScreen = Screen.Main })
                            is Screen.Logs -> LogsScreen(onBack = { currentScreen = Screen.Main })
                            is Screen.Scanner -> ScannerScreen(onResult = { barcode ->
                                // For now, create a temporary product - will be replaced with actual API call
                                val tempProduct = Product(
                                    id = barcode,
                                    name = "Scanned Item",
                                    quantity = 0,
                                    price = 0.0,
                                    barcode = barcode
                                )
                                currentScreen = Screen.Details(tempProduct)
                            }, onBack = { currentScreen = Screen.Main })
                        }
                    }
                }
            }
        }
    }
}

// Temporary implementation - will be replaced with actual repository
class FakeProductRepository : ProductRepository {
    private val products = listOf(
        Product("1", "Apple", 50, 1.2, "1234567890123"),
        Product("2", "Banana", 30, 0.8, "2345678901234"),
        Product("3", "Cherry", 20, 2.5, "3456789012345"),
        Product("4", "Orange", 40, 1.5, "4567890123456"),
        Product("5", "Mango", 15, 2.0, "5678901234567"),
        Product("6", "Pineapple", 10, 3.0, "6789012345678"),
        Product("7", "Grapes", 25, 2.8, "7890123456789"),
        Product("8", "Peach", 35, 1.8, "8901234567890")
    )

    override suspend fun getProducts() = products
    override suspend fun getProductByBarcode(barcode: String) = products.find { it.barcode == barcode }
    override suspend fun addProduct(product: Product) = true
    override suspend fun updateProduct(product: Product) = true
    override suspend fun deleteProduct(id: String) = true
    override suspend fun searchProducts(query: String) = products.filter {
        it.name.contains(query, ignoreCase = true) || it.barcode.contains(query, ignoreCase = true)
    }
}