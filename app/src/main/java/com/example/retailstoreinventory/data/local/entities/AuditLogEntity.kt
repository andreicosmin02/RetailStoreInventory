package com.example.retailstoreinventory.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Immutable audit log entry.
 * Tracks every change to products and transactions.
 * Cannot be deleted.
 */
@Entity(
    tableName = "audit_logs",
    indices = [
        Index("entity_type", "entity_id"),
        Index("timestamp")
    ]
)
data class AuditLogEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "entity_type")
    val entityType: String,  // "PRODUCT" or "TRANSACTION"

    @ColumnInfo(name = "entity_id")
    val entityId: String,

    @ColumnInfo(name = "action")
    val action: String,  // "CREATE", "UPDATE", "DELETE"

    @ColumnInfo(name = "old_value")
    val oldValue: String?,  // JSON

    @ColumnInfo(name = "new_value")
    val newValue: String?,  // JSON

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)