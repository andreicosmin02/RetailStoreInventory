package com.example.retailstoreinventory.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.retailstoreinventory.data.local.entities.InventoryStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for Inventory State table.
 * Denormalized view for fast lookups.
 */
@Dao
interface InventoryStateDao {

    @Insert
    suspend fun insert(state: InventoryStateEntity)

    @Update
    suspend fun update(state: InventoryStateEntity)

    @Query("SELECT * FROM inventory_state WHERE productId = :productId")
    suspend fun getForProduct(productId: String): InventoryStateEntity?

    /**
     * Get products below reorder threshold.
     * Used for low-stock alerts.
     */
    @Query("""
        SELECT * FROM inventory_state
        WHERE quantity_on_hand <= quantity_at_threshold
        ORDER BY quantity_on_hand ASC
    """)
    fun getLowStockProducts(): Flow<List<InventoryStateEntity>>
}
