package com.example.retailstoreinventory.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a product in the catalog.
 * This is the single source of truth for all product data.
 */
@Entity(
    tableName = "products",
    indices = [
        Index("barcode", unique = true)
    ]
)
data class ProductEntity (
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "barcode")
    val barcode: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "quantity")
    val quantity: Int,

    @ColumnInfo(name = "price")
    val price: Double,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)