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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    product: Product,
    onBack: () -> Unit,
    onRecordSale: suspend (Int, Double) -> Boolean = { _, _ -> false }
) {
    BackHandler { onBack() }

    val scope = rememberCoroutineScope()

    var currentProduct by remember { mutableStateOf(product) }
    LaunchedEffect(product.id, product.quantity, product.price, product.name, product.barcode) {
        currentProduct = product
    }

    var quantityToSell by remember { mutableStateOf("1") }
    var priceText by remember(product.id) { mutableStateOf(String.format("%.2f", product.price)) }
    var saleInProgress by remember { mutableStateOf(false) }
    var saleMessage by remember { mutableStateOf("") }

    val qtyToSell = quantityToSell.toIntOrNull() ?: 0
    val parsedPrice = parsePrice(priceText)

    val priceError = when {
        priceText.isBlank() -> "Price is required"
        parsedPrice == null -> "Enter a valid price"
        parsedPrice < 0.0 -> "Price must be >= 0"
        else -> null
    }

    val saleTotal = if (qtyToSell > 0 && parsedPrice != null) qtyToSell * parsedPrice else 0.0

    val canSubmit =
        !saleInProgress &&
                qtyToSell > 0 &&
                qtyToSell <= currentProduct.quantity &&
                parsedPrice != null &&
                priceError == null

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

                    Text(
                        text = currentProduct.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "SKU: ${currentProduct.barcode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    label = "Unit Price",
                    value = "$${String.format("%.2f", currentProduct.price)}"
                )

                StockStatusCard(quantity = currentProduct.quantity)
            }

            Spacer(modifier = Modifier.height(24.dp))

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

                    Text(
                        text = "Quantity to Sell",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val qty = quantityToSell.toIntOrNull() ?: 1
                                if (qty > 1) quantityToSell = (qty - 1).toString()
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

                        OutlinedTextField(
                            value = quantityToSell,
                            onValueChange = { newValue ->
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

                        IconButton(
                            onClick = {
                                val qty = quantityToSell.toIntOrNull() ?: 1
                                if (qty < currentProduct.quantity) quantityToSell = (qty + 1).toString()
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

                    Text(
                        text = "Price at Sale",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { input -> priceText = sanitizePriceInput(input) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                        shape = RoundedCornerShape(8.dp),
                        isError = priceError != null
                    )

                    if (priceError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = priceError)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Button(
                        onClick = {
                            if (!canSubmit) {
                                saleMessage = "✗ Invalid input"
                                return@Button
                            }
                            val price = parsedPrice
                            saleInProgress = true
                            saleMessage = ""

                            scope.launch {
                                val ok = onRecordSale(qtyToSell, price)
                                saleInProgress = false
                                if (ok) {
                                    currentProduct = currentProduct.copy(
                                        quantity = currentProduct.quantity - qtyToSell
                                    )
                                    saleMessage = "✓ Sale recorded: $qtyToSell unit(s) sold"
                                    quantityToSell = "1"
                                } else {
                                    saleMessage = "✗ Failed to record sale"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = canSubmit
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

private fun sanitizePriceInput(input: String): String {
    var s = input.filter { it.isDigit() || it == '.' || it == ',' }
    val firstSep = s.indexOfFirst { it == '.' || it == ',' }
    if (firstSep >= 0) {
        val before = s.substring(0, firstSep).filter { it.isDigit() }
        val after = s.substring(firstSep + 1).filter { it.isDigit() }
        s = (before.ifBlank { "0" }) + "." + after
    }
    if (s.isNotEmpty() && (s[0] == '.' || s[0] == ',')) s = "0$s"
    return s
}

private fun parsePrice(text: String): Double? {
    val normalized = text.trim().replace(',', '.')
    return normalized.toDoubleOrNull()
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