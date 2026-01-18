package com.example.retailstoreinventory.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Supplier(
    val id: String,
    val name: String,
    val category: String,
    val contact: String,
    val email: String,
    val phone: String,
    val location: String,
    val rating: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(onBack: () -> Unit) {
    BackHandler { onBack() }

    // Mock data - in real app, this would come from ViewModel
    val mockSuppliers = listOf(
        Supplier(
            id = "1",
            name = "Fresh Produce Co.",
            category = "Fruits & Vegetables",
            contact = "John Smith",
            email = "john@freshproduce.com",
            phone = "+1-555-0101",
            location = "Springfield",
            rating = 4.8
        ),
        Supplier(
            id = "2",
            name = "Organic Growers Inc.",
            category = "Organic Products",
            contact = "Sarah Johnson",
            email = "sarah@organicgrowers.com",
            phone = "+1-555-0102",
            location = "Shelbyville",
            rating = 4.6
        ),
        Supplier(
            id = "3",
            name = "Bulk Foods Ltd.",
            category = "Wholesale",
            contact = "Mike Davis",
            email = "mike@bulkfoods.com",
            phone = "+1-555-0103",
            location = "Capital City",
            rating = 4.5
        ),
        Supplier(
            id = "4",
            name = "Local Farmers Market",
            category = "Local Produce",
            contact = "Emma Wilson",
            email = "emma@localfarmers.com",
            phone = "+1-555-0104",
            location = "Riverside",
            rating = 4.9
        ),
        Supplier(
            id = "5",
            name = "Premium Imports",
            category = "International",
            contact = "Carlos Rodriguez",
            email = "carlos@premiumimports.com",
            phone = "+1-555-0105",
            location = "Metro City",
            rating = 4.3
        ),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suppliers") },
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
            // Header info
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
                        "Active Suppliers",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${mockSuppliers.size} suppliers",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Suppliers list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mockSuppliers) { supplier ->
                    SupplierCard(supplier = supplier)
                }
            }
        }
    }
}

@Composable
fun SupplierCard(supplier: Supplier) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with name and rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = supplier.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = supplier.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                // Rating badge
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⭐ ${String.format("%.1f", supplier.rating)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Contact info
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Contact person
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Business,
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = supplier.contact,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Email
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Email,
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = supplier.email,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Phone
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Call,
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = supplier.phone,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = supplier.location,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}