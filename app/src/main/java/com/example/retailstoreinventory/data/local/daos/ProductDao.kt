package com.example.retailstoreinventory.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.retailstoreinventory.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for Products table
 * Single source of truth for all product data
 */
@Dao
interface ProductDao {

    @Insert
    suspend fun insert(product: ProductEntity)

    @Update
    suspend fun update(product: ProductEntity)

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getById(productId: String): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode")
    suspend fun getByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<ProductEntity>>


    /**
     * Update only the quantity field.
     * Used when recording a sale.
     */
    @Query("UPDATE products SET quantity = quantity - :decrementBy, updated_at = :updatedAt WHERE id = :productId")
    suspend fun decrementQuantity(productId: String, decrementBy: Int, updatedAt: Long)

//    @Query("DELETE FROM products WHERE id = :productId")
//    suspend fun delete(productId: String)
}