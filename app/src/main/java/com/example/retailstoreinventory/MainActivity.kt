package com.example.retailstoreinventory

import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.retailstoreinventory.data.local.RetailDatabase
import com.example.retailstoreinventory.data.local.initializeSampleData
import com.example.retailstoreinventory.data.models.Product
import com.example.retailstoreinventory.ui.screens.*
import com.example.retailstoreinventory.ui.theme.RetailStoreInventoryTheme
import com.example.retailstoreinventory.ui.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

sealed class Screen {
    object Main : Screen()
    data class Details(val product: Product) : Screen()
    object Orders : Screen()
    object Providers : Screen()
    object Logs : Screen()
    object Scanner : Screen()
    data class ProductNotFound(val barcode: String) : Screen()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var database: RetailDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent {
            var isInitialized by remember { mutableStateOf(false) }
            var initError by remember { mutableStateOf<String?>(null) }

            // Initialize database on first load
            LaunchedEffect(Unit) {
                try {
                    initializeSampleData(database)
                    isInitialized = true
                } catch (e: Exception) {
                    Log.e(TAG, "Database initialization error", e)
                    initError = e.message
                }
            }

            RetailStoreInventoryTheme(darkTheme = true) {
                if (initError != null) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Error initializing database:\n${initError}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else if (!isInitialized) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    val viewModel: ProductViewModel = hiltViewModel()
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
                    var currentDetailProduct by remember { mutableStateOf<Product?>(null) }
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    val products by viewModel.products.collectAsState()

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
                                    products = products,
                                    onItemClick = { product ->
                                        currentDetailProduct = product
                                        currentScreen = Screen.Details(product)
                                    },
                                    onScanClick = { currentScreen = Screen.Scanner },
                                    onSearch = { query -> viewModel.searchProducts(query) }
                                )
                                is Screen.Details -> {
                                    currentDetailProduct?.let { product ->
                                        DetailsScreen(
                                            product = product,
                                            onBack = {
                                                currentScreen = Screen.Main
                                                currentDetailProduct = null
                                            },
                                            onRecordSale = { quantityToSell, priceAtSale ->
                                                try {
                                                    val success = viewModel.recordSale(
                                                        productId = product.id,
                                                        quantity = quantityToSell,
                                                        priceAtSale = priceAtSale
                                                    )
                                                    if (success) {
                                                        Log.d(TAG, "✓ Sale recorded: $quantityToSell units of ${product.name}")
                                                    } else {
                                                        Log.e(TAG, "✗ Failed to record sale")
                                                    }
                                                    success
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "✗ Error recording sale", e)
                                                    false
                                                }
                                            }
                                        )
                                    }
                                }
                                is Screen.Orders -> OrdersScreen(onBack = { currentScreen = Screen.Main })
                                is Screen.Providers -> ProvidersScreen(onBack = { currentScreen = Screen.Main })
                                is Screen.Logs -> LogsScreen(onBack = { currentScreen = Screen.Main })
                                is Screen.Scanner -> ScannerScreen(onResult = { barcode ->
                                    scope.launch {
                                        val foundProduct = viewModel.getProductByBarcode(barcode)
                                        if (foundProduct != null) {
                                            currentDetailProduct = foundProduct
                                            currentScreen = Screen.Details(foundProduct)
                                        } else {
                                            currentScreen = Screen.ProductNotFound(barcode)
                                        }
                                    }
                                }, onBack = { currentScreen = Screen.Main })
                                is Screen.ProductNotFound -> ProductNotFoundScreen(
                                    barcode = screen.barcode,
                                    onBack = { currentScreen = Screen.Main },
                                    onRetry = { currentScreen = Screen.Scanner }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}