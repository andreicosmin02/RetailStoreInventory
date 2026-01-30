package com.example.retailstoreinventory

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.retailstoreinventory.data.local.RetailDatabase
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ProductDao.
 * Tests CRUD operations, queries, and business logic.
 */
@RunWith(AndroidJUnit4::class)
class ProductDaoTest {

    private lateinit var database: RetailDatabase
    private lateinit var productDao: ProductDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            RetailDatabase::class.java
        )
            .allowMainThreadQueries()
            .addCallback(RetailDatabase.CALLBACK)
            .build()

        productDao = database.productDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // --- Insert Tests ---

    @Test
    fun insertProduct_insertsSuccessfully() = runTest {
        val product = createTestProduct("prod-1", "Apple", "1234567890")

        productDao.insert(product)

        val retrieved = productDao.getById("prod-1")
        assertNotNull(retrieved)
        assertEquals("Apple", retrieved?.name)
        assertEquals("1234567890", retrieved?.barcode)
    }

    @Test
    fun insertMultipleProducts_allInsertedSuccessfully() = runTest {
        val products = listOf(
            createTestProduct("prod-1", "Apple", "1111111111"),
            createTestProduct("prod-2", "Banana", "2222222222"),
            createTestProduct("prod-3", "Cherry", "3333333333")
        )

        products.forEach { productDao.insert(it) }

        val allProducts = productDao.getAll().first()
        assert(allProducts.size == 3)
    }

    // --- Update Tests ---

    @Test
    fun updateProduct_updatesSuccessfully() = runTest {
        val product = createTestProduct("prod-1", "Apple", "1234567890")
        productDao.insert(product)

        val updated = product.copy(
            name = "Red Apple",
            price = 2.50,
            quantity = 100
        )
        productDao.update(updated)

        val retrieved = productDao.getById("prod-1")
        assertEquals("Red Apple", retrieved?.name)
        assertEquals(2.50, retrieved?.price ?: 0.0, 0.001)
        assert(retrieved?.quantity == 100)
    }

    // --- Query Tests ---

    @Test
    fun getById_existingProduct_returnsProduct() = runTest {
        val product = createTestProduct("prod-1", "Apple", "1234567890")
        productDao.insert(product)

        val retrieved = productDao.getById("prod-1")

        assertNotNull(retrieved)
        assertEquals("prod-1", retrieved?.id)
    }

    @Test
    fun getById_nonExistingProduct_returnsNull() = runTest {
        val retrieved = productDao.getById("non-existent")

        assertNull(retrieved)
    }

    @Test
    fun getByBarcode_existingBarcode_returnsProduct() = runTest {
        val product = createTestProduct("prod-1", "Apple", "1234567890")
        productDao.insert(product)

        val retrieved = productDao.getByBarcode("1234567890")

        assertNotNull(retrieved)
        assertEquals("Apple", retrieved?.name)
    }

    @Test
    fun getByBarcode_nonExistingBarcode_returnsNull() = runTest {
        val retrieved = productDao.getByBarcode("9999999999")

        assertNull(retrieved)
    }

    @Test
    fun getAll_returnsAllProducts_sortedByName() = runTest {
        val products = listOf(
            createTestProduct("prod-1", "Cherry", "1111111111"),
            createTestProduct("prod-2", "Apple", "2222222222"),
            createTestProduct("prod-3", "Banana", "3333333333")
        )
        products.forEach { productDao.insert(it) }

        val retrieved = productDao.getAll().first()

        assert(retrieved.size == 3)
        assertEquals("Apple", retrieved[0].name)
        assertEquals("Banana", retrieved[1].name)
        assertEquals("Cherry", retrieved[2].name)
    }

    @Test
    fun search_byName_findsMatchingProducts() = runTest {
        val products = listOf(
            createTestProduct("prod-1", "Apple", "1111111111"),
            createTestProduct("prod-2", "Pineapple", "2222222222"),
            createTestProduct("prod-3", "Banana", "3333333333")
        )
        products.forEach { productDao.insert(it) }

        val results = productDao.search("apple").first()

        assert(results.size == 2)
        assertTrue(results.any { it.name == "Apple" })
        assertTrue(results.any { it.name == "Pineapple" })
    }

    @Test
    fun search_byBarcode_findsMatchingProducts() = runTest {
        val products = listOf(
            createTestProduct("prod-1", "Apple", "1234567890"),
            createTestProduct("prod-2", "Banana", "1234999999"),
            createTestProduct("prod-3", "Cherry", "9999999999")
        )
        products.forEach { productDao.insert(it) }

        val results = productDao.search("1234").first()

        assert(results.size == 2)
    }

    @Test
    fun search_caseInsensitive() = runTest {
        val product = createTestProduct("prod-1", "Apple", "1234567890")
        productDao.insert(product)

        val resultsLower = productDao.search("apple").first()
        val resultsUpper = productDao.search("APPLE").first()
        val resultsMixed = productDao.search("ApPlE").first()

        assert(resultsLower.size == 1)
        assert(resultsUpper.size == 1)
        assert(resultsMixed.size == 1)
    }

    // --- Quantity Management Tests ---

    @Test
    fun decrementQuantityIfEnough_sufficientStock_decrements() = runTest {
        val product = createTestProduct("prod-1", "Apple", "1234567890", quantity = 100)
        productDao.insert(product)

        val rowsAffected = productDao.decrementQuantityIfEnough(
            productId = "prod-1",
            decrementBy = 30,
            updatedAt = System.currentTimeMillis()
        )

        assert(rowsAffected == 1)
        val updated = productDao.getById("prod-1")
        assert(updated?.quantity == 70)
    }

    @Test
    fun decrementQuantityIfEnough_insufficientStock_doesNotDecrement() = runTest {
        val product = createTestProduct("prod-1", "Apple", "1234567890", quantity = 10)
        productDao.insert(product)

        val rowsAffected = productDao.decrementQuantityIfEnough(
            productId = "prod-1",
            decrementBy = 20,
            updatedAt = System.currentTimeMillis()
        )

        assert(rowsAffected == 0)
        val unchanged = productDao.getById("prod-1")
        assert(unchanged?.quantity == 10)
    }

    @Test
    fun decrementQuantityIfEnough_exactAmount_decrements() = runTest {
        val product = createTestProduct("prod-1", "Apple", "1234567890", quantity = 50)
        productDao.insert(product)

        val rowsAffected = productDao.decrementQuantityIfEnough(
            productId = "prod-1",
            decrementBy = 50,
            updatedAt = System.currentTimeMillis()
        )

        assert(rowsAffected == 1)
        val updated = productDao.getById("prod-1")
        assert(updated?.quantity == 0)
    }

    @Test
    fun incrementQuantity_increasesQuantity() = runTest {
        val product = createTestProduct("prod-1", "Apple", "1234567890", quantity = 50)
        productDao.insert(product)

        val rowsAffected = productDao.incrementQuantity(
            productId = "prod-1",
            incrementBy = 25,
            updatedAt = System.currentTimeMillis()
        )

        assert(rowsAffected == 1)
        val updated = productDao.getById("prod-1")
        assert(updated?.quantity == 75)
    }

    @Test
    fun incrementQuantity_nonExistingProduct_returnsZero() = runTest {
        val rowsAffected = productDao.incrementQuantity(
            productId = "non-existent",
            incrementBy = 10,
            updatedAt = System.currentTimeMillis()
        )

        assert(rowsAffected == 0)
    }

    // --- Delete Tests ---

    @Test
    fun deleteById_existingProduct_deletesSuccessfully() = runTest {
        val product = createTestProduct("prod-1", "Apple", "1234567890")
        productDao.insert(product)

        val rowsDeleted = productDao.deleteById("prod-1")

        assert(rowsDeleted == 1)
        val retrieved = productDao.getById("prod-1")
        assertNull(retrieved)
    }

    @Test
    fun deleteById_nonExistingProduct_returnsZero() = runTest {
        val rowsDeleted = productDao.deleteById("non-existent")

        assert(rowsDeleted == 0)
    }

    // --- Database Trigger Tests ---

    @Test
    fun insert_withNegativeQuantity_shouldFail() = runTest {
        val product = createTestProduct("prod-1", "Apple", "1234567890", quantity = -5)

        try {
            productDao.insert(product)
            fail("Should have thrown exception for negative quantity")
        } catch (e: Exception) {
            // Expected - database trigger should prevent negative quantities
            assertTrue(e.message?.contains("quantity cannot be negative") == true)
        }
    }

    @Test
    fun update_withNegativeQuantity_shouldFail() = runTest {
        val product = createTestProduct("prod-1", "Apple", "1234567890", quantity = 10)
        productDao.insert(product)

        val invalid = product.copy(quantity = -5)

        try {
            productDao.update(invalid)
            fail("Should have thrown exception for negative quantity")
        } catch (e: Exception) {
            // Expected - database trigger should prevent negative quantities
            assertTrue(e.message?.contains("quantity cannot be negative") == true)
        }
    }

    // --- Unique Constraint Tests ---

    @Test
    fun insert_duplicateBarcode_shouldFail() = runTest {
        val product1 = createTestProduct("prod-1", "Apple", "1234567890")
        val product2 = createTestProduct("prod-2", "Banana", "1234567890")

        productDao.insert(product1)

        try {
            productDao.insert(product2)
            fail("Should have thrown exception for duplicate barcode")
        } catch (e: Exception) {
            // Expected - unique constraint on barcode
            assertTrue(e.message?.contains("UNIQUE constraint") == true)
        }
    }

    // --- Helper Methods ---

    private fun createTestProduct(
        id: String,
        name: String,
        barcode: String,
        quantity: Int = 50,
        price: Double = 10.0
    ): ProductEntity {
        val now = System.currentTimeMillis()
        return ProductEntity(
            id = id,
            name = name,
            barcode = barcode,
            quantity = quantity,
            price = price,
            createdAt = now,
            updatedAt = now
        )
    }
}