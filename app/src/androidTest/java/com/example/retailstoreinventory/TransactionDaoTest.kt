package com.example.retailstoreinventory

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.retailstoreinventory.data.local.RetailDatabase
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.daos.TransactionDao
import com.example.retailstoreinventory.data.local.entities.ProductEntity
import com.example.retailstoreinventory.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for TransactionDao.
 * Tests append-only ledger operations and queries.
 */
@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var database: RetailDatabase
    private lateinit var transactionDao: TransactionDao
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

        transactionDao = database.transactionDao()
        productDao = database.productDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // --- Insert Tests ---

    @Test
    fun insertTransaction_insertsSuccessfully() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val transaction = createTestTransaction(
            id = "txn-1",
            productId = "prod-1",
            quantity = 5,
            priceAtSale = 10.0,
            total = 50.0
        )

        transactionDao.insert(transaction)

        val transactions = transactionDao.getTransactionsForProduct("prod-1").first()
        assert(transactions.size == 1)
        assertEquals("txn-1", transactions[0].id)
    }

    @Test
    fun insertMultipleTransactions_allInsertedSuccessfully() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val transactions = listOf(
            createTestTransaction("txn-1", "prod-1", 5, 10.0, 50.0, timestamp = 1000L),
            createTestTransaction("txn-2", "prod-1", 3, 10.0, 30.0, timestamp = 2000L),
            createTestTransaction("txn-3", "prod-1", 2, 10.0, 20.0, timestamp = 3000L)
        )

        transactions.forEach { transactionDao.insert(it) }

        val retrieved = transactionDao.getTransactionsForProduct("prod-1").first()
        assert(retrieved.size == 3)
    }

    // --- Query by Product Tests ---

    @Test
    fun getTransactionsForProduct_returnsCorrectTransactions() = runTest {
        val product1 = createTestProduct("prod-1", "Apple")
        val product2 = createTestProduct("prod-2", "Banana")
        productDao.insert(product1)
        productDao.insert(product2)

        transactionDao.insert(createTestTransaction("txn-1", "prod-1", 5, 10.0, 50.0))
        transactionDao.insert(createTestTransaction("txn-2", "prod-1", 3, 10.0, 30.0))
        transactionDao.insert(createTestTransaction("txn-3", "prod-2", 2, 5.0, 10.0))

        val product1Transactions = transactionDao.getTransactionsForProduct("prod-1").first()
        val product2Transactions = transactionDao.getTransactionsForProduct("prod-2").first()

        assertEquals(2, product1Transactions.size)
        assertEquals(1, product2Transactions.size)
    }

    @Test
    fun getTransactionsForProduct_orderedByDateDescending() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val transactions = listOf(
            createTestTransaction("txn-1", "prod-1", 5, 10.0, 50.0, timestamp = 1000L),
            createTestTransaction("txn-2", "prod-1", 3, 10.0, 30.0, timestamp = 3000L),
            createTestTransaction("txn-3", "prod-1", 2, 10.0, 20.0, timestamp = 2000L)
        )
        transactions.forEach { transactionDao.insert(it) }

        val retrieved = transactionDao.getTransactionsForProduct("prod-1").first()

        // Should be ordered by transaction_date DESC
        assertEquals("txn-2", retrieved[0].id) // 3000L
        assertEquals("txn-3", retrieved[1].id) // 2000L
        assertEquals("txn-1", retrieved[2].id) // 1000L
    }

    @Test
    fun getTransactionsForProduct_nonExistingProduct_returnsEmpty() = runTest {
        val transactions = transactionDao.getTransactionsForProduct("non-existent").first()

        assert(transactions.isEmpty())
    }

    // --- Query by Date Range Tests ---

    @Test
    fun getTransactionsByDateRange_returnsCorrectTransactions() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val transactions = listOf(
            createTestTransaction("txn-1", "prod-1", 5, 10.0, 50.0, timestamp = 1000L),
            createTestTransaction("txn-2", "prod-1", 3, 10.0, 30.0, timestamp = 5000L),
            createTestTransaction("txn-3", "prod-1", 2, 10.0, 20.0, timestamp = 10000L)
        )
        transactions.forEach { transactionDao.insert(it) }

        val retrieved = transactionDao.getTransactionsByDateRange(
            startDate = 2000L,
            endDate = 8000L
        ).first()

        assert(retrieved.size == 1)
        assertEquals("txn-2", retrieved[0].id)
    }

    @Test
    fun getTransactionsByDateRange_inclusiveBoundaries() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val transactions = listOf(
            createTestTransaction("txn-1", "prod-1", 5, 10.0, 50.0, timestamp = 1000L),
            createTestTransaction("txn-2", "prod-1", 3, 10.0, 30.0, timestamp = 5000L),
            createTestTransaction("txn-3", "prod-1", 2, 10.0, 20.0, timestamp = 10000L)
        )
        transactions.forEach { transactionDao.insert(it) }

        val retrieved = transactionDao.getTransactionsByDateRange(
            startDate = 1000L,
            endDate = 5000L
        ).first()

        assert(retrieved.size == 2)
    }

    // --- Revenue Calculation Tests ---

    @Test
    fun getTotalRevenue_calculatesCorrectly() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val transactions = listOf(
            createTestTransaction("txn-1", "prod-1", 5, 10.0, 50.0, timestamp = 1000L),
            createTestTransaction("txn-2", "prod-1", 3, 10.0, 30.0, timestamp = 2000L),
            createTestTransaction("txn-3", "prod-1", 2, 10.0, 20.0, timestamp = 3000L)
        )
        transactions.forEach { transactionDao.insert(it) }

        val total = transactionDao.getTotalRevenue(
            startDate = 1000L,
            endDate = 3000L
        )

        assertEquals(100.0, total, 0.001)
    }

    @Test
    fun getTotalRevenue_noTransactions_returnsZero() = runTest {
        val total = transactionDao.getTotalRevenue(
            startDate = 1000L,
            endDate = 3000L
        )

        assertEquals(0.0, total, 0.001)
    }

    @Test
    fun getTotalRevenue_partialRange_calculatesCorrectly() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val transactions = listOf(
            createTestTransaction("txn-1", "prod-1", 5, 10.0, 50.0, timestamp = 1000L),
            createTestTransaction("txn-2", "prod-1", 3, 10.0, 30.0, timestamp = 5000L),
            createTestTransaction("txn-3", "prod-1", 2, 10.0, 20.0, timestamp = 10000L)
        )
        transactions.forEach { transactionDao.insert(it) }

        val total = transactionDao.getTotalRevenue(
            startDate = 4000L,
            endDate = 12000L
        )

        assertEquals(50.0, total, 0.001) // Only txn-2 and txn-3
    }

    // --- Count Tests ---

    @Test
    fun countForProduct_returnsCorrectCount() = runTest {
        val product1 = createTestProduct("prod-1", "Apple")
        val product2 = createTestProduct("prod-2", "Banana")
        productDao.insert(product1)
        productDao.insert(product2)

        transactionDao.insert(createTestTransaction("txn-1", "prod-1", 5, 10.0, 50.0))
        transactionDao.insert(createTestTransaction("txn-2", "prod-1", 3, 10.0, 30.0))
        transactionDao.insert(createTestTransaction("txn-3", "prod-1", 2, 10.0, 20.0))
        transactionDao.insert(createTestTransaction("txn-4", "prod-2", 1, 5.0, 5.0))

        val count1 = transactionDao.countForProduct("prod-1")
        val count2 = transactionDao.countForProduct("prod-2")

        assertEquals(3, count1)
        assertEquals(1, count2)
    }

    @Test
    fun countForProduct_nonExistingProduct_returnsZero() = runTest {
        val count = transactionDao.countForProduct("non-existent")

        assert(count == 0)
    }

    // --- Foreign Key Constraint Tests ---

    @Test
    fun insertTransaction_withNonExistingProduct_shouldFail() = runTest {
        val transaction = createTestTransaction(
            id = "txn-1",
            productId = "non-existent-product",
            quantity = 5,
            priceAtSale = 10.0,
            total = 50.0
        )

        try {
            transactionDao.insert(transaction)
            fail("Should have thrown exception for foreign key constraint")
        } catch (e: Exception) {
            // Expected - foreign key constraint violation
            assertTrue(
                e.message?.contains("FOREIGN KEY constraint failed") == true ||
                        e.message?.contains("foreign key") == true
            )
        }
    }

    // --- Immutability Tests ---

    @Test
    fun transactionEntity_fieldsAreImmutable() {
        // Transaction is a data class, so it's immutable by default
        // This test documents the intended immutability
        val transaction = createTestTransaction("txn-1", "prod-1", 5, 10.0, 50.0)

        // To modify, must create a new instance
        val modified = transaction.copy(quantity = 10)

        assertNotEquals(transaction.quantity, modified.quantity)
        assert(transaction.quantity == 5) // Original unchanged
        assert(modified.quantity == 10)
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

    private fun createTestTransaction(
        id: String,
        productId: String,
        quantity: Int,
        priceAtSale: Double,
        total: Double,
        timestamp: Long = System.currentTimeMillis()
    ): TransactionEntity {
        return TransactionEntity(
            id = id,
            productId = productId,
            quantity = quantity,
            priceAtSale = priceAtSale,
            total = total,
            transactionDate = timestamp,
            createdAt = timestamp
        )
    }
}