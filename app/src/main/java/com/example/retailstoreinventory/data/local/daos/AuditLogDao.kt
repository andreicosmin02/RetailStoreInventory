package com.example.retailstoreinventory.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.retailstoreinventory.data.local.entities.AuditLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for Audit Log table.
 * Immutable: only INSERT, never UPDATE or DELETE.
 */
@Dao
interface AuditLogDao {

    /**
     * Create a new audit log entry.
     */
    @Insert
    suspend fun insert(log: AuditLogEntity)

    /**
     * Get audit history for a specific entity.
     * Shows all changes to a product or transaction.
     */
    @Query("""
        SELECT * FROM audit_logs
        WHERE entity_type = :entityType AND entity_id = :entityId
        ORDER BY timestamp DESC
    """)
    fun getHistoryForEntity(entityType: String, entityId: String): Flow<List<AuditLogEntity>>

    /**
     * Get all recent audit logs.
     * Used for the audit log screen.
     */
    @Query("""
        SELECT * FROM audit_logs
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    fun getRecentLogs(limit: Int = 100): Flow<List<AuditLogEntity>>
}