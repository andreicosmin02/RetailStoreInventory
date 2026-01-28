package com.example.retailstoreinventory.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.retailstoreinventory.data.local.entities.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(alert: AlertEntity): Long

    @Query("SELECT * FROM alerts WHERE status = :status ORDER BY created_at DESC")
    fun observeByStatus(status: String): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts ORDER BY created_at DESC")
    fun observeAll(): Flow<List<AlertEntity>>

    @Query("""
        UPDATE alerts 
        SET status = 'ACKNOWLEDGED', acknowledged_at = :acknowledgedAt 
        WHERE id = :alertId AND status = 'PENDING'
    """)
    suspend fun acknowledge(alertId: String, acknowledgedAt: Long): Int

    @Query("DELETE FROM alerts WHERE product_id = :productId")
    suspend fun deleteByProductId(productId: String): Int

}