package com.example.retailstoreinventory

import com.example.retailstoreinventory.data.local.entities.AuditLogEntity
import com.example.retailstoreinventory.data.local.entities.InventoryStateEntity
import com.example.retailstoreinventory.data.local.entities.ProductEntity
import com.example.retailstoreinventory.data.local.entities.TransactionEntity
import com.example.retailstoreinventory.data.mappers.toDomain
import com.example.retailstoreinventory.data.mappers.toEntity
import com.example.retailstoreinventory.data.models.AuditLog
import com.example.retailstoreinventory.data.models.InventoryState
import com.example.retailstoreinventory.data.models.Product
import com.example.retailstoreinventory.data.models.Transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for entity-domain mappers.
 * Tests bidirectional mapping between database entities and domain models.
 */
class MappersTest {

    // --- ProductEntity Tests ---

    @Test
    fun productEntity_toDomain_mapsAllFieldsCorrectly() {
        val entity = ProductEntity(
            id = "prod-123",
            barcode = "1234567890",
            name = "Test Product",
            quantity = 50,
            price = 19.99,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val domain = entity.toDomain()

        assertEquals("prod-123", domain.id)
        assertEquals("1234567890", domain.barcode)
        assertEquals("Test Product", domain.name)
        assertEquals(50, domain.quantity)
        assertEquals(19.99, domain.price, 0.001)
    }

    @Test
    fun product_toEntity_mapsAllFieldsCorrectly() {
        val product = Product(
            id = "prod-456",
            name = "Domain Product",
            quantity = 30,
            price = 25.50,
            barcode = "9876543210"
        )

        val entity = product.toEntity()

        assertEquals("prod-456", entity.id)
        assertEquals("9876543210", entity.barcode)
        assertEquals("Domain Product", entity.name)
        assertEquals(30, entity.quantity)
        assertEquals(25.50, entity.price, 0.001)
        assertNotNull(entity.createdAt)
        assertNotNull(entity.updatedAt)
    }

    @Test
    fun product_roundTrip_preservesData() {
        val original = Product(
            id = "prod-789",
            name = "Round Trip Product",
            quantity = 100,
            price = 99.99,
            barcode = "1111111111"
        )

        val entity = original.toEntity()
        val result = entity.toDomain()

        assertEquals(original.id, result.id)
        assertEquals(original.name, result.name)
        assertEquals(original.quantity, result.quantity)
        assertEquals(original.price, result.price, 0.001)
        assertEquals(original.barcode, result.barcode)
    }

    // --- TransactionEntity Tests ---

    @Test
    fun transactionEntity_toDomain_mapsAllFieldsCorrectly() {
        val entity = TransactionEntity(
            id = "txn-123",
            productId = "prod-123",
            quantity = 5,
            priceAtSale = 19.99,
            total = 99.95,
            transactionDate = 5000L,
            createdAt = 6000L
        )

        val domain = entity.toDomain()

        assertEquals("txn-123", domain.id)
        assertEquals("prod-123", domain.productId)
        assertEquals(5, domain.quantity)
        assertEquals(19.99, domain.priceAtSale, 0.001)
        assertEquals(99.95, domain.total, 0.001)
        assertEquals(5000L, domain.transactionDate)
        assertEquals(6000L, domain.createdAt)
    }

    @Test
    fun transaction_toEntity_mapsAllFieldsCorrectly() {
        val transaction = Transaction(
            id = "txn-456",
            productId = "prod-456",
            quantity = 10,
            priceAtSale = 5.00,
            total = 50.00,
            transactionDate = 7000L,
            createdAt = 8000L
        )

        val entity = transaction.toEntity()

        assertEquals("txn-456", entity.id)
        assertEquals("prod-456", entity.productId)
        assertEquals(10, entity.quantity)
        assertEquals(5.00, entity.priceAtSale, 0.001)
        assertEquals(50.00, entity.total, 0.001)
    }

    // --- InventoryStateEntity Tests ---

    @Test
    fun inventoryStateEntity_toDomain_mapsAllFieldsCorrectly() {
        val entity = InventoryStateEntity(
            productId = "prod-123",
            quantityOnHand = 25,
            quantityAtThreshold = 10,
            lastUpdated = 11000L
        )

        val domain = entity.toDomain()

        assertEquals("prod-123", domain.productId)
        assertEquals(25, domain.quantityOnHand)
        assertEquals(10, domain.quantityAtThreshold)
        assertEquals(11000L, domain.lastUpdated)
    }

    @Test
    fun inventoryState_toEntity_mapsAllFieldsCorrectly() {
        val state = InventoryState(
            productId = "prod-456",
            quantityOnHand = 5,
            quantityAtThreshold = 15,
            lastUpdated = 12000L
        )

        val entity = state.toEntity()

        assertEquals("prod-456", entity.productId)
        assertEquals(5, entity.quantityOnHand)
        assertEquals(15, entity.quantityAtThreshold)
        assertEquals(12000L, entity.lastUpdated)
    }

    // --- AuditLogEntity Tests ---

    @Test
    fun auditLogEntity_toDomain_mapsAllFieldsCorrectly() {
        val entity = AuditLogEntity(
            id = "audit-123",
            entityType = "PRODUCT",
            entityId = "prod-123",
            action = "CREATE",
            oldValue = null,
            newValue = """{"name": "New Product"}""",
            timestamp = 14000L
        )

        val domain = entity.toDomain()

        assertEquals("audit-123", domain.id)
        assertEquals("PRODUCT", domain.entityType)
        assertEquals("prod-123", domain.entityId)
        assertEquals("CREATE", domain.action)
        assertNull(domain.oldValue)
        assertEquals("""{"name": "New Product"}""", domain.newValue)
        assertEquals(14000L, domain.timestamp)
    }

    @Test
    fun auditLog_toEntity_mapsAllFieldsCorrectly() {
        val log = AuditLog(
            id = "audit-456",
            entityType = "TRANSACTION",
            entityId = "txn-456",
            action = "UPDATE",
            oldValue = """{"quantity": 5}""",
            newValue = """{"quantity": 10}""",
            timestamp = 15000L
        )

        val entity = log.toEntity()

        assertEquals("audit-456", entity.id)
        assertEquals("TRANSACTION", entity.entityType)
        assertEquals("txn-456", entity.entityId)
        assertEquals("UPDATE", entity.action)
        assertEquals("""{"quantity": 5}""", entity.oldValue)
        assertEquals("""{"quantity": 10}""", entity.newValue)
        assertEquals(15000L, entity.timestamp)
    }

    @Test
    fun auditLog_createAction_hasNullOldValue() {
        val log = AuditLog(
            id = "audit-create",
            entityType = "PRODUCT",
            entityId = "prod-new",
            action = "CREATE",
            oldValue = null,
            newValue = """{"name": "Brand New"}""",
            timestamp = 16000L
        )

        val entity = log.toEntity()
        val result = entity.toDomain()

        assertNull(result.oldValue)
        assertNotNull(result.newValue)
    }

    @Test
    fun auditLog_deleteAction_hasNullNewValue() {
        val log = AuditLog(
            id = "audit-delete",
            entityType = "PRODUCT",
            entityId = "prod-old",
            action = "DELETE",
            oldValue = """{"name": "Deleted Product"}""",
            newValue = null,
            timestamp = 17000L
        )

        val entity = log.toEntity()
        val result = entity.toDomain()

        assertNotNull(result.oldValue)
        assertNull(result.newValue)
    }
}