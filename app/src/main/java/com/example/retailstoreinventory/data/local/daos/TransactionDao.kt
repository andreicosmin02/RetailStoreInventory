package com.example.retailstoreinventory.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.retailstoreinventory.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for Transactions table.
 * Append-only ledger - transactions are never modified.
 */
@Dao
interface TransactionDao {

    /**
     * Record a new sale transaction.
     * This is an append-only operation.
     */
    @Insert
    suspend fun insert(transaction: TransactionEntity)

    /**
     * Get all transactions for a product.
     * Used for product history and analytics.
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE product_id = :productId 
        ORDER BY transaction_date DESC
    """)
    fun getTransactionsForProduct(productId: String): Flow<List<TransactionEntity>>

    /**
     * Get transactions within a date range.
     * Used for reporting and analytics.
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE transaction_date >= :startDate AND transaction_date <= :endDate
        ORDER BY transaction_date DESC
    """)
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    /**
     * Calculate total sales revenue for a date range.
     */
    @Query("""
        SELECT COALESCE(SUM(total), 0) FROM transactions
        WHERE transaction_date >= :startDate AND transaction_date <= :endDate
    """)
    suspend fun getTotalRevenue(startDate: Long, endDate: Long): Double
}