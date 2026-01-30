package com.example.retailstoreinventory

import com.example.retailstoreinventory.data.dtos.toDetailsDto
import com.example.retailstoreinventory.data.dtos.toDisplayDto
import com.example.retailstoreinventory.data.dtos.toLowStockAlertDto
import com.example.retailstoreinventory.data.models.AuditLog
import com.example.retailstoreinventory.data.models.InventoryState
import com.example.retailstoreinventory.data.models.Product
import com.example.retailstoreinventory.data.models.Transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for DTO mappers.
 * Tests domain model to DTO conversions, including formatting logic.
 */
class DtosMappersTest {

    // --- Product to ProductDisplayDto Tests ---

    @Test
    fun product_toDisplayDto_mapsAllFieldsCorrectly() {
        val product = Product(
            id = "prod-123",
            name = "Test Product",
            quantity = 50,
            price = 19.99,
            barcode = "1234567890"
        )

        val dto = product.toDisplayDto(isLowStock = false)

        assertEquals("prod-123", dto.id)
        assertEquals("Test Product", dto.name)
        assertEquals(50, dto.quantity)
        assertEquals(19.99, dto.price, 0.001)
        assertEquals("1234567890", dto.barcode)
        assertEquals(false, dto.isLowStock)
    }

    @Test
    fun product_toDisplayDto_withLowStockFlag_setsCorrectly() {
        val product = Product(
            id = "prod-low",
            name = "Low Stock Item",
            quantity = 5,
            price = 10.0,
            barcode = "1111111111"
        )

        val dto = product.toDisplayDto(isLowStock = true)

        assertEquals(true, dto.isLowStock)
    }

    // --- Product to ProductDetailsDto Tests ---

    @Test
    fun product_toDetailsDto_formatsPrice_correctly() {
        val product = Product(
            id = "prod-price",
            name = "Price Test",
            quantity = 100,
            price = 19.99,
            barcode = "1234567890"
        )

        val dto = product.toDetailsDto()

        assertEquals("$19.99", dto.formattedPrice)
    }

    @Test
    fun product_toDetailsDto_inStock_whenQuantityAbove10() {
        val product = Product(
            id = "prod-in-stock",
            name = "In Stock Item",
            quantity = 50,
            price = 10.0,
            barcode = "1234567890"
        )

        val dto = product.toDetailsDto()

        assertEquals("In Stock", dto.stockStatus)
    }

    @Test
    fun product_toDetailsDto_lowStock_whenQuantityBetween1And10() {
        val product = Product(
            id = "prod-low",
            name = "Low Stock Item",
            quantity = 5,
            price = 10.0,
            barcode = "1234567890"
        )

        val dto = product.toDetailsDto()

        assertEquals("Low Stock", dto.stockStatus)
    }

    @Test
    fun product_toDetailsDto_lowStock_whenQuantityExactly10() {
        val product = Product(
            id = "prod-threshold",
            name = "Threshold Item",
            quantity = 10,
            price = 10.0,
            barcode = "1234567890"
        )

        val dto = product.toDetailsDto()

        assertEquals("Low Stock", dto.stockStatus)
    }

    @Test
    fun product_toDetailsDto_outOfStock_whenQuantityZero() {
        val product = Product(
            id = "prod-out",
            name = "Out of Stock Item",
            quantity = 0,
            price = 10.0,
            barcode = "1234567890"
        )

        val dto = product.toDetailsDto()

        assertEquals("Out of Stock", dto.stockStatus)
    }

    @Test
    fun product_toDetailsDto_formatsDecimalPrices_correctly() {
        val product = Product(
            id = "prod-decimal",
            name = "Decimal Price",
            quantity = 10,
            price = 5.50,
            barcode = "1234567890"
        )

        val dto = product.toDetailsDto()

        assertEquals("$5.50", dto.formattedPrice)
    }

    @Test
    fun product_toDetailsDto_formatsWholeNumberPrices_withTwoDecimals() {
        val product = Product(
            id = "prod-whole",
            name = "Whole Price",
            quantity = 10,
            price = 10.0,
            barcode = "1234567890"
        )

        val dto = product.toDetailsDto()

        assertEquals("$10.00", dto.formattedPrice)
    }

    // --- Transaction to TransactionDisplayDto Tests ---

    @Test
    fun transaction_toDisplayDto_formatsTotal_correctly() {
        val transaction = Transaction(
            id = "txn-123",
            productId = "prod-123",
            quantity = 5,
            priceAtSale = 10.0,
            total = 50.0,
            transactionDate = 1609459200000L, // Jan 1, 2021
            createdAt = 1609459200000L
        )

        val dto = transaction.toDisplayDto("Test Product")

        assertEquals("$50.00", dto.formattedTotal)
    }

    @Test
    fun transaction_toDisplayDto_formatsDate_correctly() {
        val transaction = Transaction(
            id = "txn-date",
            productId = "prod-date",
            quantity = 1,
            priceAtSale = 10.0,
            total = 10.0,
            transactionDate = 1609459200000L,
            createdAt = 1609459200000L
        )

        val dto = transaction.toDisplayDto("Date Test")

        // Format should be "MMM dd, yyyy HH:mm"
        assertTrue(dto.formattedDate.contains("2021"))
        assertTrue(dto.formattedDate.contains("Jan") || dto.formattedDate.contains("01"))
    }

    @Test
    fun transaction_toDisplayDto_includesProductName() {
        val transaction = Transaction(
            id = "txn-name",
            productId = "prod-name",
            quantity = 2,
            priceAtSale = 15.0,
            total = 30.0,
            transactionDate = 1609459200000L,
            createdAt = 1609459200000L
        )

        val dto = transaction.toDisplayDto("Custom Product Name")

        assertEquals("Custom Product Name", dto.productName)
    }

    @Test
    fun transaction_toDisplayDto_mapsAllFields() {
        val transaction = Transaction(
            id = "txn-full",
            productId = "prod-full",
            quantity = 3,
            priceAtSale = 12.50,
            total = 37.50,
            transactionDate = 1609459200000L,
            createdAt = 1609459200000L
        )

        val dto = transaction.toDisplayDto("Full Test Product")

        assertEquals("txn-full", dto.id)
        assertEquals("Full Test Product", dto.productName)
        assertEquals(3, dto.quantity)
        assertEquals(37.50, dto.total, 0.001)
    }

    // --- AuditLog to AuditLogDisplayDto Tests ---

    @Test
    fun auditLog_toDisplayDto_createsDescription() {
        val log = AuditLog(
            id = "audit-123",
            entityType = "PRODUCT",
            entityId = "prod-123",
            action = "CREATE",
            oldValue = null,
            newValue = """{"name": "New"}""",
            timestamp = 1609459200000L
        )

        val dto = log.toDisplayDto()

        assertEquals("CREATE on PRODUCT", dto.description)
    }

    @Test
    fun auditLog_toDisplayDto_formatsTimestamp() {
        val log = AuditLog(
            id = "audit-time",
            entityType = "TRANSACTION",
            entityId = "txn-time",
            action = "UPDATE",
            oldValue = """{"old": "value"}""",
            newValue = """{"new": "value"}""",
            timestamp = 1609459200000L
        )

        val dto = log.toDisplayDto()

        // Format should be "MMM dd, yyyy HH:mm:ss"
        assertTrue(dto.formattedTimestamp.contains("2021"))
        assertTrue(dto.formattedTimestamp.contains("Jan") || dto.formattedTimestamp.contains("01"))
    }

    @Test
    fun auditLog_toDisplayDto_mapsAllFields() {
        val log = AuditLog(
            id = "audit-full",
            entityType = "PRODUCT",
            entityId = "prod-full",
            action = "DELETE",
            oldValue = """{"deleted": "data"}""",
            newValue = null,
            timestamp = 1609459200000L
        )

        val dto = log.toDisplayDto()

        assertEquals("audit-full", dto.id)
        assertEquals("PRODUCT", dto.entityType)
        assertEquals("DELETE", dto.action)
        assertEquals("""{"deleted": "data"}""", dto.oldValue)
        assertEquals(null, dto.newValue)
    }

    // --- InventoryState to LowStockAlertDto Tests ---

    @Test
    fun inventoryState_toLowStockAlertDto_createsMessage() {
        val state = InventoryState(
            productId = "prod-low",
            quantityOnHand = 5,
            quantityAtThreshold = 10,
            lastUpdated = 1609459200000L
        )

        val dto = state.toLowStockAlertDto("Test Product")

        assertEquals("Test Product is low on stock: 5 remaining", dto.message)
    }

    @Test
    fun inventoryState_toLowStockAlertDto_mapsAllFields() {
        val state = InventoryState(
            productId = "prod-alert",
            quantityOnHand = 3,
            quantityAtThreshold = 15,
            lastUpdated = 1609459200000L
        )

        val dto = state.toLowStockAlertDto("Alert Product")

        assertEquals("prod-alert", dto.productId)
        assertEquals("Alert Product", dto.productName)
        assertEquals(3, dto.currentQuantity)
        assertEquals(15, dto.threshold)
    }

    @Test
    fun inventoryState_toLowStockAlertDto_withZeroQuantity_createsMessage() {
        val state = InventoryState(
            productId = "prod-empty",
            quantityOnHand = 0,
            quantityAtThreshold = 10,
            lastUpdated = 1609459200000L
        )

        val dto = state.toLowStockAlertDto("Empty Product")

        assertEquals("Empty Product is low on stock: 0 remaining", dto.message)
    }

    // --- Edge Cases ---

    @Test
    fun product_toDetailsDto_withHighPrice_formatsCorrectly() {
        val product = Product(
            id = "prod-expensive",
            name = "Expensive Item",
            quantity = 1,
            price = 9999.99,
            barcode = "9999999999"
        )

        val dto = product.toDetailsDto()

        assertEquals("$9999.99", dto.formattedPrice)
    }

    @Test
    fun transaction_toDisplayDto_withDecimalTotal_formatsCorrectly() {
        val transaction = Transaction(
            id = "txn-decimal",
            productId = "prod-decimal",
            quantity = 3,
            priceAtSale = 3.33,
            total = 9.99,
            transactionDate = 1609459200000L,
            createdAt = 1609459200000L
        )

        val dto = transaction.toDisplayDto("Decimal Product")

        assertEquals("$9.99", dto.formattedTotal)
    }
}