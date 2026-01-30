package com.example.retailstoreinventory

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.retailstoreinventory.data.local.RetailDatabase
import com.example.retailstoreinventory.data.local.daos.AlertDao
import com.example.retailstoreinventory.data.local.daos.AuditLogDao
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.entities.AlertEntity
import com.example.retailstoreinventory.data.local.entities.AuditLogEntity
import com.example.retailstoreinventory.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for AuditLogDao.
 * Tests immutable audit logging operations.
 */
@RunWith(AndroidJUnit4::class)
class AuditLogDaoTest {

    private lateinit var database: RetailDatabase
    private lateinit var auditLogDao: AuditLogDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            RetailDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        auditLogDao = database.auditLogDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // --- Insert Tests ---

    @Test
    fun insert_insertsSuccessfully() = runTest {
        val log = createTestAuditLog(
            id = "log-1",
            entityType = "PRODUCT",
            entityId = "prod-1",
            action = "CREATE"
        )

        auditLogDao.insert(log)

        val logs = auditLogDao.getRecentLogs(100).first()
        assert(logs.size == 1)
        assertEquals("log-1", logs[0].id)
    }

    @Test
    fun insertMultipleLogs_allInsertedSuccessfully() = runTest {
        val logs = listOf(
            createTestAuditLog("log-1", "PRODUCT", "prod-1", "CREATE", timestamp = 1000L),
            createTestAuditLog("log-2", "PRODUCT", "prod-1", "UPDATE", timestamp = 2000L),
            createTestAuditLog("log-3", "TRANSACTION", "txn-1", "CREATE", timestamp = 3000L)
        )

        logs.forEach { auditLogDao.insert(it) }

        val retrieved = auditLogDao.getRecentLogs(100).first()
        assert(retrieved.size == 3)
    }

    // --- Query by Entity Tests ---

    @Test
    fun getHistoryForEntity_returnsCorrectLogs() = runTest {
        val logs = listOf(
            createTestAuditLog("log-1", "PRODUCT", "prod-1", "CREATE", timestamp = 1000L),
            createTestAuditLog("log-2", "PRODUCT", "prod-1", "UPDATE", timestamp = 2000L),
            createTestAuditLog("log-3", "PRODUCT", "prod-2", "CREATE", timestamp = 3000L),
            createTestAuditLog("log-4", "TRANSACTION", "txn-1", "CREATE", timestamp = 4000L)
        )
        logs.forEach { auditLogDao.insert(it) }

        val productLogs = auditLogDao.getHistoryForEntity("PRODUCT", "prod-1").first()

        assert(productLogs.size == 2)
        assertTrue(productLogs.all { it.entityType == "PRODUCT" && it.entityId == "prod-1" })
    }

    @Test
    fun getHistoryForEntity_orderedByTimestampDescending() = runTest {
        val logs = listOf(
            createTestAuditLog("log-1", "PRODUCT", "prod-1", "CREATE", timestamp = 1000L),
            createTestAuditLog("log-2", "PRODUCT", "prod-1", "UPDATE", timestamp = 3000L),
            createTestAuditLog("log-3", "PRODUCT", "prod-1", "UPDATE", timestamp = 2000L)
        )
        logs.forEach { auditLogDao.insert(it) }

        val history = auditLogDao.getHistoryForEntity("PRODUCT", "prod-1").first()

        assertEquals("log-2", history[0].id) // 3000L
        assertEquals("log-3", history[1].id) // 2000L
        assertEquals("log-1", history[2].id) // 1000L
    }

    // --- Recent Logs Tests ---

    @Test
    fun getRecentLogs_returnsLimitedResults() = runTest {
        repeat(150) { i ->
            auditLogDao.insert(
                createTestAuditLog("log-$i", "PRODUCT", "prod-$i", "CREATE", timestamp = i.toLong())
            )
        }

        val recent = auditLogDao.getRecentLogs(100).first()

        assert(recent.size == 100)
    }

    @Test
    fun getRecentLogs_orderedByTimestampDescending() = runTest {
        val logs = listOf(
            createTestAuditLog("log-1", "PRODUCT", "prod-1", "CREATE", timestamp = 1000L),
            createTestAuditLog("log-2", "PRODUCT", "prod-2", "CREATE", timestamp = 3000L),
            createTestAuditLog("log-3", "PRODUCT", "prod-3", "CREATE", timestamp = 2000L)
        )
        logs.forEach { auditLogDao.insert(it) }

        val recent = auditLogDao.getRecentLogs(100).first()

        assertEquals("log-2", recent[0].id)
        assertEquals("log-3", recent[1].id)
        assertEquals("log-1", recent[2].id)
    }

    // --- Immutability Tests ---

    @Test
    fun auditLog_isImmutable_cannotUpdate() {
        // AuditLogDao has no update method - this documents the immutability
        val methods = AuditLogDao::class.java.declaredMethods
        val hasUpdateMethod = methods.any { it.name == "update" }

        assertFalse("AuditLogDao should not have update method", hasUpdateMethod)
    }

    @Test
    fun auditLog_isImmutable_cannotDelete() {
        // AuditLogDao has no delete method - this documents the immutability
        val methods = AuditLogDao::class.java.declaredMethods
        val hasDeleteMethod = methods.any { it.name == "delete" }

        assertFalse("AuditLogDao should not have delete method", hasDeleteMethod)
    }

    // --- Action Types Tests ---

    @Test
    fun insert_createAction_withNullOldValue() = runTest {
        val log = createTestAuditLog(
            id = "log-create",
            entityType = "PRODUCT",
            entityId = "prod-1",
            action = "CREATE",
            oldValue = null,
            newValue = """{"name": "Apple"}"""
        )

        auditLogDao.insert(log)

        val retrieved = auditLogDao.getHistoryForEntity("PRODUCT", "prod-1").first()
        assert(retrieved.size == 1)
        assertNull(retrieved[0].oldValue)
        assertNotNull(retrieved[0].newValue)
    }

    @Test
    fun insert_updateAction_withBothValues() = runTest {
        val log = createTestAuditLog(
            id = "log-update",
            entityType = "PRODUCT",
            entityId = "prod-1",
            action = "UPDATE",
            oldValue = """{"price": 10.0}""",
            newValue = """{"price": 12.0}"""
        )

        auditLogDao.insert(log)

        val retrieved = auditLogDao.getHistoryForEntity("PRODUCT", "prod-1").first()
        assertNotNull(retrieved[0].oldValue)
        assertNotNull(retrieved[0].newValue)
    }

    @Test
    fun insert_deleteAction_withNullNewValue() = runTest {
        val log = createTestAuditLog(
            id = "log-delete",
            entityType = "PRODUCT",
            entityId = "prod-1",
            action = "DELETE",
            oldValue = """{"name": "Deleted Item"}""",
            newValue = null
        )

        auditLogDao.insert(log)

        val retrieved = auditLogDao.getHistoryForEntity("PRODUCT", "prod-1").first()
        assertNotNull(retrieved[0].oldValue)
        assertNull(retrieved[0].newValue)
    }

    // --- Helper Methods ---

    private fun createTestAuditLog(
        id: String,
        entityType: String,
        entityId: String,
        action: String,
        oldValue: String? = null,
        newValue: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ): AuditLogEntity {
        return AuditLogEntity(
            id = id,
            entityType = entityType,
            entityId = entityId,
            action = action,
            oldValue = oldValue,
            newValue = newValue,
            timestamp = timestamp
        )
    }
}

/**
 * Instrumented tests for AlertDao.
 * Tests alert management and acknowledgment.
 */
@RunWith(AndroidJUnit4::class)
class AlertDaoTest {

    private lateinit var database: RetailDatabase
    private lateinit var alertDao: AlertDao
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

        alertDao = database.alertDao()
        productDao = database.productDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // --- Insert Tests ---

    @Test
    fun insertIgnore_insertsSuccessfully() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val alert = createTestAlert("alert-1", "prod-1", "LOW_STOCK", "PENDING")
        val rowId = alertDao.insertIgnore(alert)

        assertTrue(rowId > 0)
        val alerts = alertDao.observeAll().first()
        assert(alerts.size == 1)
    }

    @Test
    fun insertIgnore_duplicateAlert_ignores() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val alert1 = createTestAlert("alert-1", "prod-1", "LOW_STOCK", "PENDING")
        val alert2 = createTestAlert("alert-2", "prod-1", "LOW_STOCK", "PENDING")

        alertDao.insertIgnore(alert1)
        val rowId2 = alertDao.insertIgnore(alert2)

        // Second insert should be ignored due to unique index
        assertEquals(-1, rowId2)

        val alerts = alertDao.observeAll().first()
        assert(alerts.size == 1)
    }

    // --- Query by Status Tests ---

    @Test
    fun observeByStatus_returnsPendingAlerts() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        alertDao.insertIgnore(createTestAlert("alert-1", "prod-1", "LOW_STOCK", "PENDING"))
        alertDao.insertIgnore(createTestAlert("alert-2", "prod-1", "OUT_OF_STOCK", "ACKNOWLEDGED"))

        val pending = alertDao.observeByStatus("PENDING").first()

        assert(pending.size == 1)
        assertEquals("PENDING", pending[0].status)
    }

    @Test
    fun observeByStatus_orderedByCreatedAtDescending() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val alerts = listOf(
            createTestAlert("alert-1", "prod-1", "LOW_STOCK", "PENDING", createdAt = 1000L),
            createTestAlert("alert-2", "prod-1", "RESTOCK_NEEDED", "PENDING", createdAt = 3000L),
            createTestAlert("alert-3", "prod-1", "THRESHOLD_REACHED", "PENDING", createdAt = 2000L)
        )

        alerts.forEach { alertDao.insertIgnore(it) }

        val pending = alertDao.observeByStatus("PENDING").first()

        assertEquals("alert-2", pending[0].id) // 3000L
        assertEquals("alert-3", pending[1].id) // 2000L
        assertEquals("alert-1", pending[2].id) // 1000L
    }

    // --- Observe All Tests ---

    @Test
    fun observeAll_returnsAllAlerts() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        alertDao.insertIgnore(createTestAlert("alert-1", "prod-1", "LOW_STOCK", "PENDING"))
        alertDao.insertIgnore(createTestAlert("alert-2", "prod-1", "OUT_OF_STOCK", "ACKNOWLEDGED"))

        val all = alertDao.observeAll().first()

        assert(all.size == 2)
    }

    // --- Acknowledge Tests ---

    @Test
    fun acknowledge_pendingAlert_acknowledgesSuccessfully() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val alert = createTestAlert("alert-1", "prod-1", "LOW_STOCK", "PENDING")
        alertDao.insertIgnore(alert)

        val acknowledgedAt = System.currentTimeMillis()
        val rowsUpdated = alertDao.acknowledge("alert-1", acknowledgedAt)

        assert(rowsUpdated == 1)

        val updated = alertDao.observeAll().first()[0]
        assertEquals("ACKNOWLEDGED", updated.status)
        assertEquals(acknowledgedAt, updated.acknowledgedAt)
    }

    @Test
    fun acknowledge_alreadyAcknowledged_doesNotUpdate() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        val alert = createTestAlert("alert-1", "prod-1", "LOW_STOCK", "ACKNOWLEDGED")
        alertDao.insertIgnore(alert)

        val acknowledgedAt = System.currentTimeMillis()
        val rowsUpdated = alertDao.acknowledge("alert-1", acknowledgedAt)

        assert(rowsUpdated == 0)
    }

    @Test
    fun acknowledge_nonExistingAlert_returnsZero() = runTest {
        val acknowledgedAt = System.currentTimeMillis()
        val rowsUpdated = alertDao.acknowledge("non-existent", acknowledgedAt)

        assert(rowsUpdated == 0)
    }

    // --- Delete by Product Tests ---

    @Test
    fun deleteByProductId_deletesAllAlertsForProduct() = runTest {
        val product1 = createTestProduct("prod-1", "Apple")
        val product2 = createTestProduct("prod-2", "Banana")
        productDao.insert(product1)
        productDao.insert(product2)

        alertDao.insertIgnore(createTestAlert("alert-1", "prod-1", "LOW_STOCK", "PENDING"))
        alertDao.insertIgnore(createTestAlert("alert-2", "prod-1", "OUT_OF_STOCK", "PENDING"))
        alertDao.insertIgnore(createTestAlert("alert-3", "prod-2", "LOW_STOCK", "PENDING"))

        val rowsDeleted = alertDao.deleteByProductId("prod-1")

        assert(rowsDeleted == 2)

        val remaining = alertDao.observeAll().first()
        assert(remaining.size == 1)
        assertEquals("prod-2", remaining[0].productId)
    }

    // --- Unique Constraint Tests ---

    @Test
    fun insert_sameProductTypeStatus_isUnique() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        // Same product_id, type, and status
        val alert1 = createTestAlert("alert-1", "prod-1", "LOW_STOCK", "PENDING")
        val alert2 = createTestAlert("alert-2", "prod-1", "LOW_STOCK", "PENDING")

        alertDao.insertIgnore(alert1)
        val result = alertDao.insertIgnore(alert2)

        // Should be ignored due to unique constraint
        assertEquals(-1, result)
    }

    @Test
    fun insert_sameProductTypeDifferentStatus_allowed() = runTest {
        val product = createTestProduct("prod-1", "Apple")
        productDao.insert(product)

        // Same product_id and type, different status
        val alert1 = createTestAlert("alert-1", "prod-1", "LOW_STOCK", "PENDING")
        val alert2 = createTestAlert("alert-2", "prod-1", "LOW_STOCK", "ACKNOWLEDGED")

        val result1 = alertDao.insertIgnore(alert1)
        val result2 = alertDao.insertIgnore(alert2)

        assertTrue(result1 > 0)
        assertTrue(result2 > 0)

        val all = alertDao.observeAll().first()
        assert(all.size == 2)
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

    private fun createTestAlert(
        id: String,
        productId: String,
        type: String,
        status: String,
        createdAt: Long = System.currentTimeMillis()
    ): AlertEntity {
        return AlertEntity(
            id = id,
            productId = productId,
            type = type,
            status = status,
            message = "Test alert message",
            payload = null,
            createdAt = createdAt,
            acknowledgedAt = null
        )
    }
}