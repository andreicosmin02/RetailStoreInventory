package com.example.retailstoreinventory.di

import android.content.Context
import androidx.room.Room
import com.example.retailstoreinventory.data.local.RetailDatabase
import com.example.retailstoreinventory.data.local.daos.AlertDao
import com.example.retailstoreinventory.data.local.daos.AuditLogDao
import com.example.retailstoreinventory.data.local.daos.InventoryStateDao
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.daos.TransactionDao
import com.example.retailstoreinventory.data.monitoring.LowStockAlertService
import com.example.retailstoreinventory.data.repository.ProductRepository
import com.example.retailstoreinventory.data.repository.ProductRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRetailDatabase(
        @ApplicationContext context: Context
    ): RetailDatabase {
        return Room.databaseBuilder(
            context,
            RetailDatabase::class.java,
            "retail.db"
        )
            .addCallback(RetailDatabase.CALLBACK)
            .addMigrations(RetailDatabase.MIGRATION_1_2, RetailDatabase.MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideProductDao(database: RetailDatabase): ProductDao = database.productDao()

    @Provides
    fun provideTransactionDao(database: RetailDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideAuditLogDao(database: RetailDatabase): AuditLogDao = database.auditLogDao()

    @Provides
    fun provideInventoryStateDao(database: RetailDatabase): InventoryStateDao = database.inventoryStateDao()

    @Provides
    fun provideAlertDao(database: RetailDatabase): AlertDao = database.alertDao()

    @Singleton
    @Provides
    fun provideProductRepository(
        database: RetailDatabase,
        productDao: ProductDao,
        transactionDao: TransactionDao,
        auditLogDao: AuditLogDao,
        inventoryStateDao: InventoryStateDao,
        lowStockAlertService: LowStockAlertService
    ): ProductRepository = ProductRepositoryImpl(
        database,
        productDao,
        transactionDao,
        auditLogDao,
        inventoryStateDao,
        lowStockAlertService
    )
}