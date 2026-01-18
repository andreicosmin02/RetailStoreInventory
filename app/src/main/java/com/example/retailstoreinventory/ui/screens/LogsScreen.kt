package com.example.retailstoreinventory.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AuditEntry(
    val id: String,
    val action: String,
    val entityType: String,
    val entityName: String,
    val details: String,
    val timestamp: Long,
    val severity: AuditSeverity
)

enum class AuditSeverity {
    INFO, WARNING, ERROR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(onBack: () -> Unit) {
    BackHandler { onBack() }

    // Mock data - in real app, this would come from ViewModel
    val mockLogs = listOf(
        AuditEntry(
            id = "1",
            action = "SALE",
            entityType = "Product",
            entityName = "Apple",
            details = "5 units sold, -$6.00",
            timestamp = System.currentTimeMillis() - 600000,
            severity = AuditSeverity.INFO
        ),
        AuditEntry(
            id = "2",
            action = "RESTOCK",
            entityType = "Product",
            entityName = "Banana",
            details = "Restocked 20 units",
            timestamp = System.currentTimeMillis() - 1800000,
            severity = AuditSeverity.INFO
        ),
        AuditEntry(
            id = "3",
            action = "LOW_STOCK_ALERT",
            entityType = "Product",
            entityName = "Mango",
            details = "Stock below threshold (5 units)",
            timestamp = System.currentTimeMillis() - 3600000,
            severity = AuditSeverity.WARNING
        ),
        AuditEntry(
            id = "4",
            action = "UPDATE",
            entityType = "Product",
            entityName = "Orange",
            details = "Price updated: $1.50 → $1.75",
            timestamp = System.currentTimeMillis() - 7200000,
            severity = AuditSeverity.INFO
        ),
        AuditEntry(
            id = "5",
            action = "OUT_OF_STOCK",
            entityType = "Product",
            entityName = "Pineapple",
            details = "Product out of stock",
            timestamp = System.currentTimeMillis() - 86400000,
            severity = AuditSeverity.WARNING
        ),
        AuditEntry(
            id = "6",
            action = "SALE",
            entityType = "Product",
            entityName = "Grapes",
            details = "3 units sold, -$8.40",
            timestamp = System.currentTimeMillis() - 86400000 - 3600000,
            severity = AuditSeverity.INFO
        ),
        AuditEntry(
            id = "7",
            action = "DELETE",
            entityType = "Product",
            entityName = "Expired Item",
            details = "Removed from inventory",
            timestamp = System.currentTimeMillis() - 172800000,
            severity = AuditSeverity.ERROR
        ),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit Logs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Header card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        "Activity Log",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${mockLogs.size} events",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Logs list
            if (mockLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.History,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No activity yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mockLogs) { log ->
                        AuditLogCard(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogCard(log: AuditEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Action icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when (log.action) {
                            "SALE" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            "RESTOCK" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            "DELETE" -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        },
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (log.action) {
                        "SALE" -> Icons.Default.Add
                        "DELETE" -> Icons.Default.Delete
                        else -> Icons.Default.Edit
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = when (log.action) {
                        "SALE" -> MaterialTheme.colorScheme.primary
                        "RESTOCK" -> Color(0xFF4CAF50)
                        "DELETE" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
            }

            // Log details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = log.entityName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(log.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            }

            // Severity indicator
            Box(
                modifier = Modifier
                    .background(
                        color = when (log.severity) {
                            AuditSeverity.INFO -> Color(0xFF2196F3).copy(alpha = 0.2f)
                            AuditSeverity.WARNING -> Color(0xFFFF9800).copy(alpha = 0.2f)
                            AuditSeverity.ERROR -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        },
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = log.severity.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (log.severity) {
                        AuditSeverity.INFO -> Color(0xFF2196F3)
                        AuditSeverity.WARNING -> Color(0xFFFF9800)
                        AuditSeverity.ERROR -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}