package com.example.retailstoreinventory.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.retailstoreinventory.data.local.entities.InventoryStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: InventoryStateEntity)

    @Update
    suspend fun update(state: InventoryStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: InventoryStateEntity)

    @Query("SELECT * FROM inventory_state WHERE productId = :productId")
    suspend fun getForProduct(productId: String): InventoryStateEntity?

    @Query("""
        SELECT * FROM inventory_state
        WHERE quantity_on_hand <= quantity_at_threshold
        ORDER BY quantity_on_hand ASC
    """)
    fun getLowStockProducts(): Flow<List<InventoryStateEntity>>

    @Query("""
        SELECT * FROM inventory_state
        WHERE quantity_on_hand <= quantity_at_threshold
        ORDER BY quantity_on_hand ASC
    """)
    suspend fun getLowStockProductsOnce(): List<InventoryStateEntity>

    @Query("SELECT * FROM inventory_state")
    suspend fun getAllOnce(): List<InventoryStateEntity>

    @Query("SELECT COUNT(*) FROM inventory_state")
    suspend fun countOnce(): Int
}