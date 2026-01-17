package com.example.retailstoreinventory.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single transaction (sale).
 * Immutable: once created, never modified.
 * This is an append-only ledger.
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index("product_id"),
        Index("transaction_date")
    ],
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "product_id")
    val productId: String,

    @ColumnInfo(name = "quantity")
    val quantity: Int,

    @ColumnInfo(name = "price_at_sale")
    val priceAtSale: Double,

    @ColumnInfo(name = "total")
    val total: Double,

    @ColumnInfo(name = "transaction_date")
    val transactionDate: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)