package com.example.retailstoreinventory.data.models

data class AuditLog(
    val id: String,
    val entityType: String, // "PRODUCT", "TRANSACTION"
    val entityId: String,
    val action: String, // "CREATE", "UPDATE", "DELETE"
    val oldValue: String?, // JSON
    val newValue: String?, // JSON
    val timestamp: Long
)