package com.example.retailstoreinventory.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.retailstoreinventory.data.models.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    product: Product,
    onBack: () -> Unit,
    onRecordSale: (Int) -> Unit = {}
) {
    BackHandler { onBack() }

    var quantityToSell by remember { mutableStateOf("1") }
    var saleInProgress by remember { mutableStateOf(false) }
    var saleMessage by remember { mutableStateOf("") }
    var currentProduct by remember { mutableStateOf(product) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Product Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Product Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentProduct.name.first().toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Product Name
                    Text(
                        text = currentProduct.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Barcode
                    Text(
                        text = "SKU: ${currentProduct.barcode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Product Info Cards
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Price Card
                InfoCard(
                    label = "Unit Price",
                    value = "$${String.format("%.2f", currentProduct.price)}"
                )

                // Stock Status Card
                StockStatusCard(quantity = currentProduct.quantity)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sale Recording Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Record Sale",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quantity Input
                    Text(
                        text = "Quantity to Sell",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quantity selector with +/- buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Minus button
                        IconButton(
                            onClick = {
                                val qty = quantityToSell.toIntOrNull() ?: 1
                                if (qty > 1) {
                                    quantityToSell = (qty - 1).toString()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(Icons.Default.Remove, null)
                        }

                        // Quantity input field
                        OutlinedTextField(
                            value = quantityToSell,
                            onValueChange = { newValue ->
                                // Only allow numbers
                                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                    quantityToSell = newValue
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                textAlign = TextAlign.Center
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Plus button
                        IconButton(
                            onClick = {
                                val qty = quantityToSell.toIntOrNull() ?: 1
                                if (qty < currentProduct.quantity) {
                                    quantityToSell = (qty + 1).toString()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(Icons.Default.Add, null)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sale total calculation
                    val qtyToSell = quantityToSell.toIntOrNull() ?: 0
                    val saleTotal = if (qtyToSell > 0) qtyToSell * currentProduct.price else 0.0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sale Total:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$${String.format("%.2f", saleTotal)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Record Sale Button
                    Button(
                        onClick = {
                            val qty = quantityToSell.toIntOrNull() ?: 0
                            if (qty > 0 && qty <= currentProduct.quantity) {
                                saleInProgress = true
                                // Call the callback to record the sale in the database
                                onRecordSale(qty)

                                // Update local UI
                                currentProduct = currentProduct.copy(
                                    quantity = currentProduct.quantity - qty
                                )
                                saleMessage = "✓ Sale recorded: $qty unit(s) sold"
                                quantityToSell = "1"
                                saleInProgress = false
                            } else {
                                saleMessage = "✗ Invalid quantity (max: ${currentProduct.quantity})"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !saleInProgress && (quantityToSell.toIntOrNull() ?: 0) > 0 && (quantityToSell.toIntOrNull() ?: 0) <= currentProduct.quantity
                    ) {
                        if (saleInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Record Sale", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Sale message feedback
                    if (saleMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = saleMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (saleMessage.startsWith("✓")) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (saleMessage.startsWith("✓")) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.errorContainer
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun InfoCard(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainer,
                RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StockStatusCard(quantity: Int) {
    val (statusText, statusColor) = when {
        quantity <= 0 -> "Out of Stock" to MaterialTheme.colorScheme.error
        quantity <= 10 -> "Low Stock" to MaterialTheme.colorScheme.errorContainer
        else -> "In Stock" to MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                statusColor.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Current Stock",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "$quantity units",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )
    }
}