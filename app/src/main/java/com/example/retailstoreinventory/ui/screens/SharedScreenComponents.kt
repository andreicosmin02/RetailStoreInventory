package com.example.retailstoreinventory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GenericPlaceholderScreen(title: String, subtitle: String, onBack: () -> Unit) {
    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Info, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onBack, shape = MaterialTheme.shapes.small) { Text("Return to Home") }
        }
    }
}