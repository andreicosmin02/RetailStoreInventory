package com.example.retailstoreinventory.data.models

sealed class InventoryCommand {
    data class Sale(val productId: String, val quantity: Int, val price: Double) : InventoryCommand()
    data class ReceiveOrder(val productId: String, val quantity: Int) : InventoryCommand()
}