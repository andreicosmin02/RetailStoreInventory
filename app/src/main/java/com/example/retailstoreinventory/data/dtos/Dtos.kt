package com.example.retailstoreinventory.data.dtos

/**
 * UI-specific data transfer objects
 * These are shaped specifically for what the UI needs to display
 */

// DTO for Product display in lists
data class ProductDisplayDto(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    val barcode: String,
    val isLowStock: Boolean = false
)

// DTO for Product details screen
data class ProductDetailsDto(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    val barcode: String,
    val formattedPrice: String,
    val stockStatus: String // "In Stock", "Low Stock", "Out of Stock"
)

// DTO for Transaction display
data class TransactionDisplayDto(
    val id: String,
    val productName: String,
    val quantity: Int,
    val total: Double,
    val formattedTotal: String,
    val formattedDate: String
)

// DTO for Audit Log display
data class AuditLogDisplayDto(
    val id: String,
    val entityType: String,
    val action: String,
    val description: String, // Human-readable description
    val formattedTimestamp: String,
    val oldValue: String?,
    val newValue: String?
)

// DTO for Low Stock Alert
data class LowStockAlertDto(
    val productId: String,
    val productName: String,
    val currentQuantity: Int,
    val threshold: Int,
    val message: String // "Product X is low on stock: 5 remaining"
)