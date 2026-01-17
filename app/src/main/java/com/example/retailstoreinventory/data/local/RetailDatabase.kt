package com.example.retailstoreinventory.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.retailstoreinventory.data.local.daos.AuditLogDao
import com.example.retailstoreinventory.data.local.daos.InventoryStateDao
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.daos.TransactionDao
import com.example.retailstoreinventory.data.local.entities.AuditLogEntity
import com.example.retailstoreinventory.data.local.entities.InventoryStateEntity
import com.example.retailstoreinventory.data.local.entities.ProductEntity
import com.example.retailstoreinventory.data.local.entities.TransactionEntity

@Database(
    entities = [
        ProductEntity::class,
        TransactionEntity::class,
        AuditLogEntity::class,
        InventoryStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RetailDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun inventoryStateDao(): InventoryStateDao

    companion object {
        @Volatile
        private var instance: RetailDatabase? = null

        fun getInstance(context: Context): RetailDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): RetailDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                RetailDatabase::class.java,
                "retail.db"
            ).build()
        }
    }
}
