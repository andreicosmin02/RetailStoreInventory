package com.example.retailstoreinventory.data.local

import kotlinx.coroutines.flow.first
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.entities.ProductEntity
import java.util.UUID

/**
 * Initializes the database with sample products on first launch.
 * Run this once before the app loads the main screen.
 */
suspend fun initializeSampleData(database: RetailDatabase) {
    val productDao = database.productDao()

    // Check if database already has products
    val shouldInsertSampleData = try {
        productDao.getAll().first().isEmpty() // Get the first emission and check if empty
    } catch (e: Exception) {
        true // If there's an error, assume we should insert samples
    }

    // Only insert sample data if no products exist
    if (shouldInsertSampleData) {
        val sampleProducts = listOf(
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "1234567890123",
                name = "Apple",
                quantity = 50,
                price = 1.2,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "2345678901234",
                name = "Banana",
                quantity = 30,
                price = 0.8,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "3456789012345",
                name = "Cherry",
                quantity = 20,
                price = 2.5,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "4567890123456",
                name = "Orange",
                quantity = 40,
                price = 1.5,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "5678901234567",
                name = "Mango",
                quantity = 15,
                price = 2.0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "6789012345678",
                name = "Pineapple",
                quantity = 10,
                price = 3.0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "7890123456789",
                name = "Grapes",
                quantity = 25,
                price = 2.8,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "8901234567890",
                name = "Peach",
                quantity = 35,
                price = 1.8,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        // Insert all sample products
        sampleProducts.forEach { product ->
            productDao.insert(product)
        }
    }
}