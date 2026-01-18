package com.example.retailstoreinventory.di

import android.content.Context
import androidx.room.Room
import com.example.retailstoreinventory.data.local.RetailDatabase
import com.example.retailstoreinventory.data.local.daos.AuditLogDao
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.daos.TransactionDao
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
        ).build()
    }

    @Provides
    fun provideProductDao(database: RetailDatabase): ProductDao = database.productDao()

    @Provides
    fun provideTransactionDao(database: RetailDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideAuditLogDao(database: RetailDatabase): AuditLogDao = database.auditLogDao()

    @Singleton
    @Provides
    fun provideProductRepository(
        productDao: ProductDao,
        transactionDao: TransactionDao,
        auditLogDao: AuditLogDao
    ): ProductRepository = ProductRepositoryImpl(productDao, transactionDao, auditLogDao)
}