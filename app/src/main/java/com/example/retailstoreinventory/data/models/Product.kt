package com.example.retailstoreinventory.data.models

data class Product(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    val barcode: String,
    val category: String? = null,
    val description: String? = null
) {
    init {
        require(quantity >= 0) { "Quantity cannot be negative" }
        require(price >= 0) { "Price cannot be negative" }
        require(barcode.isNotBlank()) { "Barcode cannot be empty" }
        require(name.isNotBlank()) { "Product name cannot be empty" }
    }
}