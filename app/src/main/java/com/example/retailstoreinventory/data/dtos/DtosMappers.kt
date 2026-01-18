package com.example.retailstoreinventory.data.dtos

import android.R.attr.action
import com.example.retailstoreinventory.data.models.AuditLog
import com.example.retailstoreinventory.data.models.InventoryState
import com.example.retailstoreinventory.data.models.Product
import com.example.retailstoreinventory.data.models.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Extension functions to map domain models to DTOs
 * These handle formatting and UI-specific transformations
 */

fun Product.toDisplayDto(isLowStock: Boolean = false): ProductDisplayDto = ProductDisplayDto(
    id = id,
    name = name,
    quantity = quantity,
    price = price,
    barcode = barcode,
    isLowStock = isLowStock
)

fun Product.toDetailsDto(): ProductDetailsDto {
    val formattedPrice = String.format(Locale.US, "$%.2f", price)
    val stockStatus = when {
        quantity <= 0 -> "Out of Stock"
        quantity <= 10 -> "Low Stock"
        else -> "In Stock"
    }

    return ProductDetailsDto(
        id = id,
        name = name,
        quantity = quantity,
        price = price,
        barcode = barcode,
        formattedPrice = formattedPrice,
        stockStatus = stockStatus
    )
}

fun Transaction.toDisplayDto(productName: String): TransactionDisplayDto {
    val formattedTotal = String.format(Locale.US, "$%.2f", total)
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
    val formattedDate = dateFormatter.format(Date(transactionDate))

    return TransactionDisplayDto(
        id = id,
        productName = productName,
        quantity = quantity,
        total = total,
        formattedTotal = formattedTotal,
        formattedDate = formattedDate
    )
}

fun AuditLog.toDisplayDto(): AuditLogDisplayDto {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US)
    val formattedTimestamp = dateFormatter.format(Date(timestamp))

    val description = "$action on $entityType"

    return AuditLogDisplayDto(
        id = id,
        entityType = entityType,
        action = action,
        description = description,
        formattedTimestamp = formattedTimestamp,
        oldValue = oldValue,
        newValue = newValue
    )
}

fun InventoryState.toLowStockAlertDto(productName: String): LowStockAlertDto {
    val message = "$productName is low on stock: $quantityOnHand remaining"

    return LowStockAlertDto(
        productId = productId,
        productName = productName,
        currentQuantity = quantityOnHand,
        threshold = quantityAtThreshold,
        message = message
    )
}
