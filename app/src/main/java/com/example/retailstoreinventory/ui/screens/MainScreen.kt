package com.example.retailstoreinventory.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.retailstoreinventory.data.models.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    products: List<Product>,
    onItemClick: (Product) -> Unit,
    onScanClick: () -> Unit,
    onSearch: (String) -> Unit
) {
    val localFocusManager = LocalFocusManager.current
    val searchBarState = rememberTextFieldState()
    var filteredProducts by remember { mutableStateOf(products) }

    LaunchedEffect(searchBarState.text) {
        onSearch(searchBarState.text.toString())
        filteredProducts = if (searchBarState.text.toString().isBlank()) products
        else products.filter {
            it.name.contains(searchBarState.text.toString(), ignoreCase = true) ||
                    it.barcode.contains(searchBarState.text.toString(), ignoreCase = true)
        }
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
                        shape = MaterialTheme.shapes.extraLarge,
                        lineLimits = TextFieldLineLimits.SingleLine
                    )
                    FilledTonalIconButton(
                        onClick = {
                            filteredProducts = filteredProducts.sortedBy { it.name }
                            localFocusManager.clearFocus()
                        },
                        modifier = Modifier.size(56.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Icon(Icons.Default.SortByAlpha, null)
                    }
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredProducts) { product ->
                    InventoryCard(product = product, onClick = { onItemClick(product) })
                }
            }
        }
    }
}

@Composable
fun InventoryCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(MaterialTheme.shapes.small)
                    // Use .background instead of .content
                    .background(color = MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.name.first().toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Available: ${product.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}