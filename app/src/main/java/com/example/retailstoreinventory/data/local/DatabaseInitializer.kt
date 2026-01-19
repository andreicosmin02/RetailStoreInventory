package com.example.retailstoreinventory.data.local

import com.example.retailstoreinventory.data.local.entities.InventoryStateEntity
import com.example.retailstoreinventory.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.first
import java.util.UUID

suspend fun initializeSampleData(database: RetailDatabase) {
    val productDao = database.productDao()
    val inventoryStateDao = database.inventoryStateDao()

    val shouldInsertSampleData = try {
        productDao.getAll().first().isEmpty()
    } catch (e: Exception) {
        true
    }

    if (shouldInsertSampleData) {
        val now = System.currentTimeMillis()

        val sampleProducts = listOf(
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "1234567890123",
                name = "Apple",
                quantity = 50,
                price = 1.2,
                createdAt = now,
                updatedAt = now
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "2345678901234",
                name = "Banana",
                quantity = 30,
                price = 0.8,
                createdAt = now,
                updatedAt = now
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "3456789012345",
                name = "Cherry",
                quantity = 20,
                price = 2.5,
                createdAt = now,
                updatedAt = now
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "4567890123456",
                name = "Orange",
                quantity = 40,
                price = 1.5,
                createdAt = now,
                updatedAt = now
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "5678901234567",
                name = "Mango",
                quantity = 15,
                price = 2.0,
                createdAt = now,
                updatedAt = now
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "6789012345678",
                name = "Pineapple",
                quantity = 10,
                price = 3.0,
                createdAt = now,
                updatedAt = now
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "7890123456789",
                name = "Grapes",
                quantity = 25,
                price = 2.8,
                createdAt = now,
                updatedAt = now
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "8901234567890",
                name = "Peach",
                quantity = 35,
                price = 1.8,
                createdAt = now,
                updatedAt = now
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                barcode = "4980416",
                name = "Test Product (Barcode Scanner Demo)",
                quantity = 100,
                price = 9.99,
                createdAt = now,
                updatedAt = now
            )
        )

        sampleProducts.forEach { product ->
            productDao.insert(product)
        }

        sampleProducts.forEachIndexed { index, product ->
            val threshold = 10
            val onHand = product.quantity
            inventoryStateDao.upsert(
                InventoryStateEntity(
                    productId = product.id,
                    quantityOnHand = onHand,
                    quantityAtThreshold = threshold,
                    lastUpdated = now
                )
            )
        }
    } else {
        val count = inventoryStateDao.countOnce()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val existingProducts = productDao.getAll().first()

            existingProducts.forEachIndexed { index, p ->
                val threshold = 10
                val onHand = p.quantity
                inventoryStateDao.upsert(
                    InventoryStateEntity(
                        productId = p.id,
                        quantityOnHand = onHand,
                        quantityAtThreshold = threshold,
                        lastUpdated = now
                    )
                )
            }
        }
    }
}