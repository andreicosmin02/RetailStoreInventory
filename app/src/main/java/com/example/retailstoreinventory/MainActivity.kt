package com.example.retailstoreinventory

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.retailstoreinventory.ui.theme.RetailStoreInventoryTheme
import kotlinx.coroutines.launch

sealed class Screen {
    object Main : Screen()
    data class Details(val name: String) : Screen()
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
                            is Screen.Main -> MainScreen(onItemClick = { name -> currentScreen = Screen.Details(name) }, onScanClick = { currentScreen = Screen.Scanner })
                            is Screen.Details -> DetailsScreen(itemName = screen.name, onBack = { currentScreen = Screen.Main })
                            is Screen.Orders -> OrdersScreen(onBack = { currentScreen = Screen.Main })
                            is Screen.Providers -> ProvidersScreen(onBack = { currentScreen = Screen.Main })
                            is Screen.Logs -> LogsScreen(onBack = { currentScreen = Screen.Main })
                            is Screen.Scanner -> ScannerScreen(onResult = { barcode -> currentScreen = Screen.Details(barcode) }, onBack = { currentScreen = Screen.Main })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onItemClick: (String) -> Unit, onScanClick: () -> Unit) {
    val localFocusManager = LocalFocusManager.current
    val myItems = remember { listOf("Apple", "Banana", "Cherry", "Orange", "Mango", "Pineapple", "Grapes", "Peach") }
    val searchBarState = rememberTextFieldState()
    val filteredMyItemsState = remember { mutableStateOf(myItems) }

    LaunchedEffect(searchBarState.text) {
        filteredMyItemsState.value = if (searchBarState.text.isBlank()) myItems
        else myItems.filter { it.contains(searchBarState.text, ignoreCase = true) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onScanClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.QrCodeScanner, null) },
                text = { Text("Scan Product") }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().clickable(null, null) { localFocusManager.clearFocus() }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Inventory", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        state = searchBarState,
                        placeholder = { Text("Search stocks...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        lineLimits = TextFieldLineLimits.SingleLine
                    )
                    FilledTonalIconButton(
                        onClick = { filteredMyItemsState.value = filteredMyItemsState.value.sorted(); localFocusManager.clearFocus() },
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.SortByAlpha, null)
                    }
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredMyItemsState.value) { item -> InventoryCard(name = item, onClick = { onItemClick(item) }) }
            }
        }
    }
}

@Composable
fun ScannerScreen(onResult: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasCamPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCamPermission = it }
    val scanner = remember { BarcodeScanner() }

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = ""
    )

    LaunchedEffect(Unit) { launcher.launch(Manifest.permission.CAMERA) }
    BackHandler { onBack() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCamPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                        val selector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
                        val imageAnalysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                        var isScanned = false
                        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                            val result = scanner.processImage(imageProxy)
                            if (result != null && !isScanned) { isScanned = true; onResult(result) }
                            imageProxy.close()
                        }
                        try { cameraProvider.unbindAll(); cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis) } catch (e: Exception) { e.printStackTrace() }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(280.dp, 160.dp).border(2.dp, Color.Cyan, RoundedCornerShape(12.dp))) {
                        Box(modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopCenter).offset(y = (160 * scanLineY).dp).background(Color.Cyan))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Align barcode inside frame", color = Color.White, fontWeight = FontWeight.Medium)
                }
            }
        }
        IconButton(onClick = onBack, modifier = Modifier.statusBarsPadding().padding(16.dp).background(Color.Black.copy(0.5f), CircleShape)) {
            Icon(Icons.Default.Close, null, tint = Color.White)
        }
    }
}

@Composable
fun InventoryCard(name: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                Text(text = name.first().toString(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Available in stock", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun DetailsScreen(itemName: String, onBack: () -> Unit) {
    BackHandler { onBack() }
    GenericPlaceholderScreen(title = itemName, subtitle = "SKU: ${itemName.hashCode().toString().take(8)}", onBack = onBack)
}

@Composable
fun OrdersScreen(onBack: () -> Unit) = GenericPlaceholderScreen("Active Orders", "Track incoming shipments", onBack)
@Composable
fun ProvidersScreen(onBack: () -> Unit) = GenericPlaceholderScreen("Suppliers", "Contact local distributors", onBack)
@Composable
fun LogsScreen(onBack: () -> Unit) = GenericPlaceholderScreen("Audit Logs", "History of stock changes", onBack)

@Composable
fun GenericPlaceholderScreen(title: String, subtitle: String, onBack: () -> Unit) {
    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Info, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onBack, shape = RoundedCornerShape(12.dp)) { Text("Return to Home") }
        }
    }
}