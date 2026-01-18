package com.example.retailstoreinventory.data.models

data class Transaction(
    val id: String,
    val productId: String,
    val quantity: Int,
    val priceAtSale: Double,
    val total: Double,
    val transactionDate: Long,
    val createdAt: Long
) {
    init {
        require(quantity > 0) { "Quantity must be greater than 0" }
        require(priceAtSale > 0) { "Price must be greater than 0" }
        require(total > 0) { "Total must be greater than 0" }
    }
}