package com.example.retailstoreinventory.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alerts",
    indices = [
        Index(value = ["product_id", "type", "status"], unique = true),
        Index(value = ["status"]),
        Index(value = ["created_at"])
    ]
)
data class AlertEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "product_id")
    val productId: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "payload")
    val payload: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "acknowledged_at")
    val acknowledgedAt: Long?
)