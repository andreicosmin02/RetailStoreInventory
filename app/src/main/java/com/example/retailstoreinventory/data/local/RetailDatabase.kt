package com.example.retailstoreinventory.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.retailstoreinventory.data.local.daos.AlertDao
import com.example.retailstoreinventory.data.local.daos.AuditLogDao
import com.example.retailstoreinventory.data.local.daos.InventoryStateDao
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.daos.TransactionDao
import com.example.retailstoreinventory.data.local.entities.AlertEntity
import com.example.retailstoreinventory.data.local.entities.AuditLogEntity
import com.example.retailstoreinventory.data.local.entities.InventoryStateEntity
import com.example.retailstoreinventory.data.local.entities.ProductEntity
import com.example.retailstoreinventory.data.local.entities.TransactionEntity

@Database(
    entities = [
        ProductEntity::class,
        TransactionEntity::class,
        InventoryStateEntity::class,
        AuditLogEntity::class,
        AlertEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class RetailDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun inventoryStateDao(): InventoryStateDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun alertDao(): AlertDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS alerts (
                        id TEXT NOT NULL,
                        product_id TEXT NOT NULL,
                        type TEXT NOT NULL,
                        status TEXT NOT NULL,
                        message TEXT NOT NULL,
                        payload TEXT,
                        created_at INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )

                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_alerts_product_id_type_status ON alerts(product_id, type, status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_alerts_status ON alerts(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_alerts_created_at ON alerts(created_at)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alerts ADD COLUMN acknowledged_at INTEGER")
            }
        }

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