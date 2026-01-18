package com.example.retailstoreinventory.data.repository

import com.example.retailstoreinventory.data.models.InventoryChangeEvent
import com.example.retailstoreinventory.data.models.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun addProduct(product: Product): Boolean
    suspend fun updateProduct(product: Product): Boolean
    suspend fun deleteProduct(id: String): Boolean
    suspend fun searchProducts(query: String): List<Product>
    fun observeInventoryChanges(): Flow<InventoryChangeEvent>
}