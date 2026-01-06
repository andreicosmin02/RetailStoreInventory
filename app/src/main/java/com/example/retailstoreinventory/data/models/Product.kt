package com.example.retailstoreinventory.data.models

data class Product(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    val barcode: String,
    val category: String? = null,
    val description: String? = null
)