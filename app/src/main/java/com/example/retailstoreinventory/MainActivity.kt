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
import com.example.retailstoreinventory.data.local.RetailDatabase
import com.example.retailstoreinventory.data.local.daos.AuditLogDao
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.daos.TransactionDao
import com.example.retailstoreinventory.data.local.entities.AuditLogEntity
import com.example.retailstoreinventory.data.local.entities.ProductEntity
import com.example.retailstoreinventory.data.local.entities.TransactionEntity
import com.example.retailstoreinventory.data.local.initializeSampleData
import com.example.retailstoreinventory.data.models.Product
import com.example.retailstoreinventory.data.repository.ProductRepository
import com.example.retailstoreinventory.data.repository.ProductRepositoryImpl
import com.example.retailstoreinventory.ui.screens.*
import com.example.retailstoreinventory.ui.theme.RetailStoreInventoryTheme
import com.example.retailstoreinventory.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

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
            val database = RetailDatabase.getInstance(this@MainActivity)
            val productDao = database.productDao()
            val transactionDao = database.transactionDao()
            val auditLogDao = database.auditLogDao()

            var isInitialized by remember { mutableStateOf(false) }
            var initError by remember { mutableStateOf<String?>(null) }

            // Initialize database on first load
            LaunchedEffect(Unit) {
                try {
                    initializeSampleData(database)
                    isInitialized = true
                } catch (e: Exception) {
                    initError = e.message
                }
            }

            RetailStoreInventoryTheme(darkTheme = true) {
                if (initError != null) {
                    // Show error screen
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text(
                                text = "Error initializing database:\n${initError}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else if (!isInitialized) {
                    // Loading screen - wait for database to be ready
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    // Only create ViewModel AFTER database is initialized
                    val repository = ProductRepositoryImpl(
                        productDao = productDao,
                        transactionDao = transactionDao,
                        auditLogDao = auditLogDao
                    )
                    val viewModel = remember { ProductViewModel(repository) }

                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    // Collect products from ViewModel's StateFlow
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
                                    onItemClick = { product -> currentScreen = Screen.Details(product) },
                                    onScanClick = { currentScreen = Screen.Scanner },
                                    onSearch = { query -> viewModel.searchProducts(query) }
                                )
                                is Screen.Details -> DetailsScreen(product = screen.product, onBack = { currentScreen = Screen.Main })
                                is Screen.Orders -> OrdersScreen(onBack = { currentScreen = Screen.Main })
                                is Screen.Providers -> ProvidersScreen(onBack = { currentScreen = Screen.Main })
                                is Screen.Logs -> LogsScreen(onBack = { currentScreen = Screen.Main })
                                is Screen.Scanner -> ScannerScreen(onResult = { barcode ->
                                    scope.launch {
                                        val foundProduct = repository.getProductByBarcode(barcode)
                                        if (foundProduct != null) {
                                            currentScreen = Screen.Details(foundProduct)
                                        }
                                    }
                                }, onBack = { currentScreen = Screen.Main })
                            }
                        }
                    }
                }
            }
        }
    }
}
