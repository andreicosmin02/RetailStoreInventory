package com.example.retailstoreinventory.data.monitoring

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.retailstoreinventory.data.local.daos.AlertDao
import com.example.retailstoreinventory.data.local.daos.InventoryStateDao
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.entities.AlertEntity
import com.example.retailstoreinventory.data.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.UUID

@HiltWorker
class LowStockMonitorWorker @AssistedInject constructor(
    private val inventoryStateDao: InventoryStateDao,
    private val alertDao: AlertDao,
    private val productDao: ProductDao,
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        android.util.Log.d("LowStockMonitorWorker", "Worker started")

        val lowStock = inventoryStateDao.getLowStockProductsOnce()
        val now = System.currentTimeMillis()
        val notifier = NotificationHelper(applicationContext)

        for (item in lowStock) {
            val product = productDao.getById(item.productId)
            val name = product?.name ?: "Unknown product"

            val id = UUID.randomUUID().toString()
            val payload = """{"quantityOnHand":${item.quantityOnHand},"threshold":${item.quantityAtThreshold}}"""
            val message = "Low stock for $name (${item.quantityOnHand}/${item.quantityAtThreshold})"

            val inserted = alertDao.insertIgnore(
                AlertEntity(
                    id = id,
                    productId = item.productId,
                    type = "LOW_STOCK",
                    status = "PENDING",
                    message = message,
                    payload = payload,
                    createdAt = now,
                    acknowledgedAt = null
                )
            )

            if (inserted != -1L) {
                notifier.showLowStock(id, message)
            }
        }

        return Result.success()
    }
}