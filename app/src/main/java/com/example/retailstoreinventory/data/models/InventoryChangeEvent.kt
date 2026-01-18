package com.example.retailstoreinventory.data.models

data class InventoryChangeEvent(
    val productId: String,
    val oldQuantity: Int,
    val newQuantity: Int,
    val action: String,
    val timestamp: Long
)