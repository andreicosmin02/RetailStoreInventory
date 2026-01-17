package com.example.retailstoreinventory.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


/**
 * Denormalized inventory state view.
 * Allows fast lookups without joins.
 * Synced from products table.
 */
@Entity(
    tableName = "inventory_state",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class InventoryStateEntity(
    @PrimaryKey
    val productId: String,

    @ColumnInfo(name = "quantity_on_hand")
    val quantityOnHand: Int,

    @ColumnInfo(name = "quantity_at_threshold")
    val quantityAtThreshold: Int = 10,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long
)