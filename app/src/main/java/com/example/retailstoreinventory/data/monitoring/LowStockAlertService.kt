package com.example.retailstoreinventory.data.monitoring

import android.content.Context
import com.example.retailstoreinventory.data.local.daos.AlertDao
import com.example.retailstoreinventory.data.local.daos.InventoryStateDao
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.entities.AlertEntity
import com.example.retailstoreinventory.data.notifications.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LowStockAlertService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val inventoryStateDao: InventoryStateDao,
    private val alertDao: AlertDao,
    private val productDao: ProductDao
) {
    suspend fun checkProduct(productId: String) {
        val state = inventoryStateDao.getForProduct(productId) ?: return
        if (state.quantityOnHand > state.quantityAtThreshold) return

        val product = productDao.getById(productId)
        val name = product?.name ?: "Unknown product"

        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val payload = """{"quantityOnHand":${state.quantityOnHand},"threshold":${state.quantityAtThreshold}}"""
        val message = "Low stock for $name (${state.quantityOnHand}/${state.quantityAtThreshold})"

        val inserted = alertDao.insertIgnore(
            AlertEntity(
                id = id,
                productId = productId,
                type = "LOW_STOCK",
                status = "PENDING",
                message = message,
                payload = payload,
                createdAt = now,
                acknowledgedAt = null
            )
        )

        if (inserted != -1L) {
            NotificationHelper(context).showLowStock(id, message)
        }
    }
}