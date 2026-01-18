package com.example.retailstoreinventory.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
        InventoryStateEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RetailDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun inventoryStateDao(): InventoryStateDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        val CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                createTriggers(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                createTriggers(db)
            }

            private fun createTriggers(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS trg_products_no_negative_insert
                    BEFORE INSERT ON products
                    WHEN NEW.quantity < 0
                    BEGIN
                        SELECT RAISE(ABORT, 'quantity cannot be negative');
                    END;
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS trg_products_no_negative_update
                    BEFORE UPDATE OF quantity ON products
                    WHEN NEW.quantity < 0
                    BEGIN
                        SELECT RAISE(ABORT, 'quantity cannot be negative');
                    END;
                    """.trimIndent()
                )
            }
        }
    }
}