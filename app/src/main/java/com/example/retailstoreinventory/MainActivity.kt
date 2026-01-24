package com.example.retailstoreinventory

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.retailstoreinventory.data.local.RetailDatabase
import com.example.retailstoreinventory.data.local.initializeSampleData
import com.example.retailstoreinventory.data.models.Product
import com.example.retailstoreinventory.data.monitoring.LowStockMonitorWorker
import com.example.retailstoreinventory.ui.screens.DetailsScreen
import com.example.retailstoreinventory.ui.screens.LogsScreen
import com.example.retailstoreinventory.ui.screens.MainScreen
import com.example.retailstoreinventory.ui.screens.OrdersScreen
import com.example.retailstoreinventory.ui.screens.ProductNotFoundScreen
import com.example.retailstoreinventory.ui.screens.ProvidersScreen
import com.example.retailstoreinventory.ui.screens.ScannerScreen
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

            val workManager = WorkManager.getInstance(this@MainActivity)

            fun enqueueInitialLowStockScan() {
                val req = OneTimeWorkRequestBuilder<LowStockMonitorWorker>().build()
                workManager.enqueueUniqueWork(
                    "low_stock_initial_scan",
                    ExistingWorkPolicy.REPLACE,
                    req
                )
            }

            val notificationLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { granted ->
                    if (granted && initError == null && isInitialized) {
                        enqueueInitialLowStockScan()
                    }
                }
            )

            LaunchedEffect(Unit) {
                try {
                    initializeSampleData(database)
                    isInitialized = true
                } catch (e: Exception) {
                    Log.e(TAG, "Database initialization error", e)
                    initError = e.message
                }

                if (initError == null) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        val granted = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            enqueueInitialLowStockScan()
                        } else {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        enqueueInitialLowStockScan()
                    }
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
                                    Text(
                                        "RetailPro",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                Spacer(Modifier.height(12.dp))
                                NavigationDrawerItem(
                                    icon = { androidx.compose.material3.Icon(Icons.Default.Inventory, null) },
                                    label = { Text("Inventory") },
                                    selected = currentScreen is Screen.Main,
                                    onClick = { currentScreen = Screen.Main; scope.launch { drawerState.close() } },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                NavigationDrawerItem(
                                    icon = { androidx.compose.material3.Icon(Icons.Default.ShoppingCart, null) },
                                    label = { Text("Orders") },
                                    selected = currentScreen is Screen.Orders,
                                    onClick = { currentScreen = Screen.Orders; scope.launch { drawerState.close() } },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                NavigationDrawerItem(
                                    icon = { androidx.compose.material3.Icon(Icons.Default.Business, null) },
                                    label = { Text("Providers") },
                                    selected = currentScreen is Screen.Providers,
                                    onClick = { currentScreen = Screen.Providers; scope.launch { drawerState.close() } },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                NavigationDrawerItem(
                                    icon = { androidx.compose.material3.Icon(Icons.Default.History, null) },
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

                                    onAddProduct = { product ->
                                        viewModel.addProduct(product)
                                    },

                                    onSearch = { query -> viewModel.searchProducts(query) },
                                    onSortClick = { viewModel.toggleSortOrder() }
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

                                is Screen.Scanner -> ScannerScreen(
                                    onResult = { barcode ->
                                        scope.launch {
                                            val foundProduct = viewModel.getProductByBarcode(barcode)
                                            if (foundProduct != null) {
                                                currentDetailProduct = foundProduct
                                                currentScreen = Screen.Details(foundProduct)
                                            } else {
                                                currentScreen = Screen.ProductNotFound(barcode)
                                            }
                                        }
                                    },
                                    onBack = { currentScreen = Screen.Main }
                                )

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