package com.example.retailstoreinventory.data.models

data class InventoryState(
    val productId: String,
    val quantityOnHand: Int,
    val quantityAtThreshold: Int = 10,
    val lastUpdated: Long
) {
    init {
        require(quantityOnHand >= 0) { "Quantity on hand cannot be negative" }
        require(quantityAtThreshold > 0) { "Threshold must be greater than 0" }
    }

    fun isLowStock(): Boolean = quantityOnHand <= quantityAtThreshold
}