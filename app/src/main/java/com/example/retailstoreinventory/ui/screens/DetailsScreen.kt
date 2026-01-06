package com.example.retailstoreinventory.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.example.retailstoreinventory.data.models.Product

@Composable
fun DetailsScreen(product: Product, onBack: () -> Unit) {
    BackHandler { onBack() }
    GenericPlaceholderScreen(title = product.name, subtitle = "SKU: ${product.barcode}", onBack = onBack)
}