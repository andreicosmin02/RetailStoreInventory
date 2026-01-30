package com.example.retailstoreinventory

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.retailstoreinventory.data.local.RetailDatabase
import com.example.retailstoreinventory.data.local.daos.InventoryStateDao
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.entities.InventoryStateEntity
import com.example.retailstoreinventory.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for InventoryStateDao.
 * Tests inventory state tracking and low stock queries.
 */
@RunWith(AndroidJUnit4::class)
class InventoryStateDaoTest {

    private lateinit var database: RetailDatabase
    private lateinit var inventoryStateDao: InventoryStateDao
    private lateinit var productDao: ProductDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            RetailDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        inventoryStateDao = database.inventoryStateDao()
        productDao = database.productDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // --- Insert Tests ---

    @Test
    fun insert_insertsSuccessfully() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val state = createTestInventoryState("prod-1", quantityOnHand = 50, threshold = 10)
        inventoryStateDao.insert(state)

        val retrieved = inventoryStateDao.getForProduct("prod-1")
        assertNotNull(retrieved)
        assert(retrieved?.quantityOnHand == 50)
        assert(retrieved?.quantityAtThreshold == 10)
    }

    @Test
    fun insert_withReplace_replacesExisting() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val state1 = createTestInventoryState("prod-1", quantityOnHand = 50, threshold = 10)
        inventoryStateDao.insert(state1)

        val state2 = createTestInventoryState("prod-1", quantityOnHand = 30, threshold = 15)
        inventoryStateDao.insert(state2)

        val retrieved = inventoryStateDao.getForProduct("prod-1")
        assert(retrieved?.quantityOnHand == 30)
        assert(retrieved?.quantityAtThreshold == 15)
    }

    // --- Update Tests ---

    @Test
    fun update_updatesSuccessfully() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val state = createTestInventoryState("prod-1", quantityOnHand = 50, threshold = 10)
        inventoryStateDao.insert(state)

        val updated = state.copy(quantityOnHand = 25, lastUpdated = System.currentTimeMillis())
        inventoryStateDao.update(updated)

        val retrieved = inventoryStateDao.getForProduct("prod-1")
        assert(retrieved?.quantityOnHand == 25)
    }

    // --- Upsert Tests ---

    @Test
    fun upsert_insertsWhenNotExists() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val state = createTestInventoryState("prod-1", quantityOnHand = 50, threshold = 10)
        inventoryStateDao.upsert(state)

        val retrieved = inventoryStateDao.getForProduct("prod-1")
        assertNotNull(retrieved)
        assert(retrieved?.quantityOnHand == 50)
    }

    @Test
    fun upsert_updatesWhenExists() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val state1 = createTestInventoryState("prod-1", quantityOnHand = 50, threshold = 10)
        inventoryStateDao.upsert(state1)

        val state2 = createTestInventoryState("prod-1", quantityOnHand = 30, threshold = 10)
        inventoryStateDao.upsert(state2)

        val retrieved = inventoryStateDao.getForProduct("prod-1")
        assert(retrieved?.quantityOnHand == 30)

        // Should still be only one record
        val count = inventoryStateDao.countOnce()
        assert(count == 1)
    }

    // --- Query Tests ---

    @Test
    fun getForProduct_existingProduct_returnsState() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val state = createTestInventoryState("prod-1", quantityOnHand = 50, threshold = 10)
        inventoryStateDao.insert(state)

        val retrieved = inventoryStateDao.getForProduct("prod-1")

        assertNotNull(retrieved)
        assertEquals("prod-1", retrieved?.productId)
    }

    @Test
    fun getForProduct_nonExistingProduct_returnsNull() = runTest {
        val retrieved = inventoryStateDao.getForProduct("non-existent")

        assertNull(retrieved)
    }

    // --- Low Stock Query Tests ---

    @Test
    fun getLowStockProducts_returnsProductsBelowThreshold() = runTest {
        setupMultipleProducts()

        val lowStock = inventoryStateDao.getLowStockProducts().first()

        assert(lowStock.size == 3)
        assertTrue(lowStock.all { it.quantityOnHand <= it.quantityAtThreshold })
    }

    @Test
    fun getLowStockProducts_orderedByQuantityAscending() = runTest {
        val products = listOf(
            createTestProduct("prod-1", "Product1"),
            createTestProduct("prod-2", "Product2"),
            createTestProduct("prod-3", "Product3")
        )
        products.forEach { productDao.insert(it) }

        val states = listOf(
            createTestInventoryState("prod-1", quantityOnHand = 8, threshold = 10),
            createTestInventoryState("prod-2", quantityOnHand = 3, threshold = 10),
            createTestInventoryState("prod-3", quantityOnHand = 5, threshold = 10)
        )
        states.forEach { inventoryStateDao.insert(it) }

        val lowStock = inventoryStateDao.getLowStockProducts().first()

        assertEquals(3, lowStock[0].quantityOnHand)
        assertEquals(5, lowStock[1].quantityOnHand)
        assertEquals(8, lowStock[2].quantityOnHand)
    }

    @Test
    fun getLowStockProducts_includesProductsAtThreshold() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val state = createTestInventoryState("prod-1", quantityOnHand = 10, threshold = 10)
        inventoryStateDao.insert(state)

        val lowStock = inventoryStateDao.getLowStockProducts().first()

        assert(lowStock.size == 1)
    }

    @Test
    fun getLowStockProducts_excludesProductsAboveThreshold() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val state = createTestInventoryState("prod-1", quantityOnHand = 50, threshold = 10)
        inventoryStateDao.insert(state)

        val lowStock = inventoryStateDao.getLowStockProducts().first()

        assert(lowStock.isEmpty())
    }

    @Test
    fun getLowStockProductsOnce_returnsCorrectProducts() = runTest {
        setupMultipleProducts()

        val lowStock = inventoryStateDao.getLowStockProductsOnce()

        assert(lowStock.size == 3)
    }

    // --- Get All Tests ---

    @Test
    fun getAllOnce_returnsAllProducts() = runTest {
        setupMultipleProducts()

        val all = inventoryStateDao.getAllOnce()

        assert(all.size == 5)
    }

    // --- Count Tests ---

    @Test
    fun countOnce_returnsCorrectCount() = runTest {
        setupMultipleProducts()

        val count = inventoryStateDao.countOnce()

        assert(count == 5)
    }

    @Test
    fun countOnce_emptyDatabase_returnsZero() = runTest {
        val count = inventoryStateDao.countOnce()

        assert(count == 0)
    }

    // --- Foreign Key Cascade Tests ---

    @Test
    fun deleteProduct_cascadesDeleteToInventoryState() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val state = createTestInventoryState("prod-1", quantityOnHand = 50, threshold = 10)
        inventoryStateDao.insert(state)

        // Delete the product
        productDao.deleteById("prod-1")

        // Inventory state should also be deleted due to CASCADE
        val retrieved = inventoryStateDao.getForProduct("prod-1")
        assertNull(retrieved)
    }

    // --- Edge Cases ---

    @Test
    fun insert_zeroQuantity_insertsSuccessfully() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val state = createTestInventoryState("prod-1", quantityOnHand = 0, threshold = 10)
        inventoryStateDao.insert(state)

        val retrieved = inventoryStateDao.getForProduct("prod-1")
        assert(retrieved?.quantityOnHand == 0)
    }

    @Test
    fun getLowStockProducts_zeroQuantity_included() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val state = createTestInventoryState("prod-1", quantityOnHand = 0, threshold = 10)
        inventoryStateDao.insert(state)

        val lowStock = inventoryStateDao.getLowStockProducts().first()

        assert(lowStock.size == 1)
        assertEquals(0, lowStock[0].quantityOnHand)
    }

    @Test
    fun insert_customThreshold_usesCorrectValue() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val state = createTestInventoryState("prod-1", quantityOnHand = 25, threshold = 20)
        inventoryStateDao.insert(state)

        val retrieved = inventoryStateDao.getForProduct("prod-1")
        assert(retrieved?.quantityAtThreshold == 20)

        // Should be in low stock since 25 > 20
        val lowStock = inventoryStateDao.getLowStockProducts().first()
        assert(lowStock.isEmpty())
    }

    @Test
    fun insert_defaultThreshold_uses10() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        // Using default threshold (10)
        val state = InventoryStateEntity(
            productId = "prod-1",
            quantityOnHand = 50,
            lastUpdated = System.currentTimeMillis()
        )
        inventoryStateDao.insert(state)

        val retrieved = inventoryStateDao.getForProduct("prod-1")
        assert(retrieved?.quantityAtThreshold == 10)
    }

    // --- Helper Methods ---

    private fun createTestProduct(id: String, name: String): ProductEntity {
        val now = System.currentTimeMillis()
        return ProductEntity(
            id = id,
            name = name,
            barcode = "barcode-$id",
            quantity = 100,
            price = 10.0,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun createTestInventoryState(
        productId: String,
        quantityOnHand: Int,
        threshold: Int
    ): InventoryStateEntity {
        return InventoryStateEntity(
            productId = productId,
            quantityOnHand = quantityOnHand,
            quantityAtThreshold = threshold,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private suspend fun setupMultipleProducts() {
        val products = listOf(
            createTestProduct("prod-1", "LowStock1"),
            createTestProduct("prod-2", "LowStock2"),
            createTestProduct("prod-3", "AtThreshold"),
            createTestProduct("prod-4", "InStock"),
            createTestProduct("prod-5", "HighStock")
        )
        products.forEach { productDao.insert(it) }

        val states = listOf(
            createTestInventoryState("prod-1", quantityOnHand = 5, threshold = 10),
            createTestInventoryState("prod-2", quantityOnHand = 3, threshold = 10),
            createTestInventoryState("prod-3", quantityOnHand = 10, threshold = 10),
            createTestInventoryState("prod-4", quantityOnHand = 15, threshold = 10),
            createTestInventoryState("prod-5", quantityOnHand = 50, threshold = 10)
        )
        states.forEach { inventoryStateDao.insert(it) }
    }
}