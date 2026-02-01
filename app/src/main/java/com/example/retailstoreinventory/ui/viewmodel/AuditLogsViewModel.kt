package com.example.retailstoreinventory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailstoreinventory.data.local.daos.AuditLogDao
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.ui.screens.AuditEntry
import com.example.retailstoreinventory.ui.screens.AuditSeverity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuditLogsViewModel @Inject constructor(
    private val auditLogDao: AuditLogDao,
    private val productDao: ProductDao
) : ViewModel() {

    private val _logs = MutableStateFlow<List<AuditEntry>>(emptyList())
    val logs: StateFlow<List<AuditEntry>> = _logs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadLogs() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Fetch last 100 audit logs
                auditLogDao.getRecentLogs(100).collect { auditLogEntities ->
                    val entries = auditLogEntities.map { log ->
                        val severity = determineSeverity(log.action)
                        val entityName = getEntityName(log.entityType, log.entityId)

                        AuditEntry(
                            id = log.id,
                            action = log.action,
                            entityType = log.entityType,
                            entityName = entityName,
                            details = formatDetails(
                                log.action,
                                log.entityType,
                                log.oldValue,
                                log.newValue
                            ),
                            timestamp = log.timestamp,
                            severity = severity
                        )
                    }

                    _logs.value = entries
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _logs.value = emptyList()
            }
        }
    }

    private suspend fun getEntityName(entityType: String, entityId: String): String {
        return when (entityType) {
            "PRODUCT" -> {
                try {
                    productDao.getById(entityId)?.name ?: "Unknown Product"
                } catch (e: Exception) {
                    "Unknown Product"
                }
            }
            "TRANSACTION" -> "Transaction"
            "INVENTORY" -> "Inventory"
            else -> entityType
        }
    }

    private fun formatDetails(
        action: String,
        entityType: String,
        oldValue: String?,
        newValue: String?
    ): String {
        return when (action) {
            "CREATE" -> "Created new $entityType"
            "UPDATE" -> "Updated $entityType information"
            "DELETE" -> "Deleted $entityType"
            "SALE" -> {
                // Parse sale details from newValue JSON
                try {
                    if (newValue != null && newValue.contains("sold=")) {
                        val regex = """sold=(\d+).*price=([0-9.]+).*total=([0-9.]+)""".toRegex()
                        val match = regex.find(newValue)
                        if (match != null) {
                            val qty = match.groupValues[1]
                            val total = match.groupValues[3]
                            "$qty units sold, $$total revenue"
                        } else {
                            "Sale transaction recorded"
                        }
                    } else {
                        "Sale transaction recorded"
                    }
                } catch (e: Exception) {
                    "Sale transaction recorded"
                }
            }
            "ORDER_RECEIVED" -> {
                try {
                    if (newValue != null && newValue.contains("received=")) {
                        val regex = """received=(\d+)""".toRegex()
                        val match = regex.find(newValue)
                        if (match != null) {
                            val qty = match.groupValues[1]
                            "Received $qty units from supplier"
                        } else {
                            "Order received and stock updated"
                        }
                    } else {
                        "Order received and stock updated"
                    }
                } catch (e: Exception) {
                    "Order received and stock updated"
                }
            }
            else -> "$action operation"
        }
    }

    private fun determineSeverity(action: String): AuditSeverity {
        return when (action) {
            "DELETE" -> AuditSeverity.ERROR
            "LOW_STOCK_ALERT", "OUT_OF_STOCK" -> AuditSeverity.WARNING
            else -> AuditSeverity.INFO
        }
    }
}