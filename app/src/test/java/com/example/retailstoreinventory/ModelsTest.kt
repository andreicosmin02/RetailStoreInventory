package com.example.retailstoreinventory

import com.example.retailstoreinventory.data.models.InventoryChangeEvent
import com.example.retailstoreinventory.data.models.InventoryCommand
import com.example.retailstoreinventory.data.models.InventoryMutationResult
import com.example.retailstoreinventory.data.models.InventoryState
import com.example.retailstoreinventory.data.models.Product
import com.example.retailstoreinventory.data.models.Transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for domain models.
 * Tests validation logic and business rules.
 */
class ModelsTest {

    // --- Product Tests ---

    @Test
    fun product_withValidData_createsSuccessfully() {
        val product = Product(
            id = "prod-123",
            name = "Test Product",
            quantity = 10,
            price = 19.99,
            barcode = "1234567890"
        )

        assertEquals("prod-123", product.id)
        assertEquals("Test Product", product.name)
        assertEquals(10, product.quantity)
        assertEquals(19.99, product.price, 0.001)
        assertEquals("1234567890", product.barcode)
    }

    @Test(expected = IllegalArgumentException::class)
    fun product_withNegativeQuantity_throwsException() {
        Product(
            id = "prod-invalid",
            name = "Invalid Product",
            quantity = -1,
            price = 10.0,
            barcode = "1234567890"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun product_withNegativePrice_throwsException() {
        Product(
            id = "prod-invalid",
            name = "Invalid Product",
            quantity = 10,
            price = -5.0,
            barcode = "1234567890"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun product_withEmptyBarcode_throwsException() {
        Product(
            id = "prod-invalid",
            name = "Invalid Product",
            quantity = 10,
            price = 10.0,
            barcode = ""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun product_withBlankBarcode_throwsException() {
        Product(
            id = "prod-invalid",
            name = "Invalid Product",
            quantity = 10,
            price = 10.0,
            barcode = "   "
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun product_withEmptyName_throwsException() {
        Product(
            id = "prod-invalid",
            name = "",
            quantity = 10,
            price = 10.0,
            barcode = "1234567890"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun product_withBlankName_throwsException() {
        Product(
            id = "prod-invalid",
            name = "   ",
            quantity = 10,
            price = 10.0,
            barcode = "1234567890"
        )
    }

    @Test
    fun product_withZeroQuantity_isValid() {
        val product = Product(
            id = "prod-zero",
            name = "Out of Stock",
            quantity = 0,
            price = 10.0,
            barcode = "1234567890"
        )

        assertEquals(0, product.quantity)
    }

    @Test
    fun product_withZeroPrice_isValid() {
        val product = Product(
            id = "prod-free",
            name = "Free Item",
            quantity = 10,
            price = 0.0,
            barcode = "1234567890"
        )

        assertEquals(0.0, product.price, 0.001)
    }

    @Test
    fun product_withOptionalFields_createsSuccessfully() {
        val product = Product(
            id = "prod-optional",
            name = "Product with Extras",
            quantity = 10,
            price = 10.0,
            barcode = "1234567890",
            category = "Electronics",
            description = "A test product"
        )

        assertEquals("Electronics", product.category)
        assertEquals("A test product", product.description)
    }

    // --- Transaction Tests ---

    @Test
    fun transaction_withValidData_createsSuccessfully() {
        val transaction = Transaction(
            id = "txn-123",
            productId = "prod-123",
            quantity = 5,
            priceAtSale = 10.0,
            total = 50.0,
            transactionDate = 1000L,
            createdAt = 1000L
        )

        assertEquals("txn-123", transaction.id)
        assertEquals("prod-123", transaction.productId)
        assertEquals(5, transaction.quantity)
        assertEquals(10.0, transaction.priceAtSale, 0.001)
        assertEquals(50.0, transaction.total, 0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun transaction_withZeroQuantity_throwsException() {
        Transaction(
            id = "txn-invalid",
            productId = "prod-123",
            quantity = 0,
            priceAtSale = 10.0,
            total = 0.0,
            transactionDate = 1000L,
            createdAt = 1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun transaction_withNegativeQuantity_throwsException() {
        Transaction(
            id = "txn-invalid",
            productId = "prod-123",
            quantity = -1,
            priceAtSale = 10.0,
            total = -10.0,
            transactionDate = 1000L,
            createdAt = 1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun transaction_withZeroPrice_throwsException() {
        Transaction(
            id = "txn-invalid",
            productId = "prod-123",
            quantity = 5,
            priceAtSale = 0.0,
            total = 0.0,
            transactionDate = 1000L,
            createdAt = 1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun transaction_withNegativePrice_throwsException() {
        Transaction(
            id = "txn-invalid",
            productId = "prod-123",
            quantity = 5,
            priceAtSale = -10.0,
            total = -50.0,
            transactionDate = 1000L,
            createdAt = 1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun transaction_withZeroTotal_throwsException() {
        Transaction(
            id = "txn-invalid",
            productId = "prod-123",
            quantity = 5,
            priceAtSale = 10.0,
            total = 0.0,
            transactionDate = 1000L,
            createdAt = 1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun transaction_withNegativeTotal_throwsException() {
        Transaction(
            id = "txn-invalid",
            productId = "prod-123",
            quantity = 5,
            priceAtSale = 10.0,
            total = -50.0,
            transactionDate = 1000L,
            createdAt = 1000L
        )
    }

    @Test
    fun transaction_withDecimalValues_createsSuccessfully() {
        val transaction = Transaction(
            id = "txn-decimal",
            productId = "prod-decimal",
            quantity = 3,
            priceAtSale = 3.33,
            total = 9.99,
            transactionDate = 1000L,
            createdAt = 1000L
        )

        assertEquals(3.33, transaction.priceAtSale, 0.001)
        assertEquals(9.99, transaction.total, 0.001)
    }

    // --- InventoryState Tests ---

    @Test
    fun inventoryState_withValidData_createsSuccessfully() {
        val state = InventoryState(
            productId = "prod-123",
            quantityOnHand = 50,
            quantityAtThreshold = 10,
            lastUpdated = 1000L
        )

        assertEquals("prod-123", state.productId)
        assertEquals(50, state.quantityOnHand)
        assertEquals(10, state.quantityAtThreshold)
        assertEquals(1000L, state.lastUpdated)
    }

    @Test(expected = IllegalArgumentException::class)
    fun inventoryState_withNegativeQuantity_throwsException() {
        InventoryState(
            productId = "prod-invalid",
            quantityOnHand = -1,
            quantityAtThreshold = 10,
            lastUpdated = 1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun inventoryState_withZeroThreshold_throwsException() {
        InventoryState(
            productId = "prod-invalid",
            quantityOnHand = 50,
            quantityAtThreshold = 0,
            lastUpdated = 1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun inventoryState_withNegativeThreshold_throwsException() {
        InventoryState(
            productId = "prod-invalid",
            quantityOnHand = 50,
            quantityAtThreshold = -5,
            lastUpdated = 1000L
        )
    }

    @Test
    fun inventoryState_withZeroQuantity_isValid() {
        val state = InventoryState(
            productId = "prod-zero",
            quantityOnHand = 0,
            quantityAtThreshold = 10,
            lastUpdated = 1000L
        )

        assertEquals(0, state.quantityOnHand)
    }

    @Test
    fun inventoryState_isLowStock_whenQuantityBelowThreshold() {
        val state = InventoryState(
            productId = "prod-low",
            quantityOnHand = 5,
            quantityAtThreshold = 10,
            lastUpdated = 1000L
        )

        assertTrue(state.isLowStock())
    }

    @Test
    fun inventoryState_isLowStock_whenQuantityAtThreshold() {
        val state = InventoryState(
            productId = "prod-threshold",
            quantityOnHand = 10,
            quantityAtThreshold = 10,
            lastUpdated = 1000L
        )

        assertTrue(state.isLowStock())
    }

    @Test
    fun inventoryState_isNotLowStock_whenQuantityAboveThreshold() {
        val state = InventoryState(
            productId = "prod-good",
            quantityOnHand = 50,
            quantityAtThreshold = 10,
            lastUpdated = 1000L
        )

        assertFalse(state.isLowStock())
    }

    @Test
    fun inventoryState_isLowStock_whenQuantityIsZero() {
        val state = InventoryState(
            productId = "prod-empty",
            quantityOnHand = 0,
            quantityAtThreshold = 10,
            lastUpdated = 1000L
        )

        assertTrue(state.isLowStock())
    }

    @Test
    fun inventoryState_withDefaultThreshold_usesCorrectValue() {
        val state = InventoryState(
            productId = "prod-default",
            quantityOnHand = 50,
            lastUpdated = 1000L
        )

        assertEquals(10, state.quantityAtThreshold)
    }

    // --- InventoryCommand Tests ---

    @Test
    fun inventoryCommand_sale_createsCorrectly() {
        val command = InventoryCommand.Sale(
            productId = "prod-123",
            quantity = 5,
            price = 10.0
        )

        assertTrue(command is InventoryCommand.Sale)
        assertEquals("prod-123", command.productId)
        assertEquals(5, command.quantity)
        assertEquals(10.0, command.price, 0.001)
    }

    @Test
    fun inventoryCommand_receiveOrder_createsCorrectly() {
        val command = InventoryCommand.ReceiveOrder(
            productId = "prod-123",
            quantity = 100
        )

        assertTrue(command is InventoryCommand.ReceiveOrder)
        assertEquals("prod-123", command.productId)
        assertEquals(100, command.quantity)
    }

    // --- InventoryMutationResult Tests ---

    @Test
    fun inventoryMutationResult_ok_isCorrectType() {
        val result = InventoryMutationResult.Ok

        assertTrue(result is InventoryMutationResult.Ok)
    }

    @Test
    fun inventoryMutationResult_error_containsMessage() {
        val result = InventoryMutationResult.Error("Insufficient stock")

        assertTrue(result is InventoryMutationResult.Error)
        assertEquals("Insufficient stock", result.message)
    }

    // --- InventoryChangeEvent Tests ---

    @Test
    fun inventoryChangeEvent_createsWithAllFields() {
        val event = InventoryChangeEvent(
            productId = "prod-123",
            oldQuantity = 50,
            newQuantity = 45,
            action = "SALE",
            timestamp = 1000L
        )

        assertEquals("prod-123", event.productId)
        assertEquals(50, event.oldQuantity)
        assertEquals(45, event.newQuantity)
        assertEquals("SALE", event.action)
        assertEquals(1000L, event.timestamp)
    }
}