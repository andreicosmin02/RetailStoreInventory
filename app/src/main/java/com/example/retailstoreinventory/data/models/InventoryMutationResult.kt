package com.example.retailstoreinventory.data.models

sealed class InventoryMutationResult {
    data object Ok : InventoryMutationResult()
    data class Error(val message: String) : InventoryMutationResult()
}