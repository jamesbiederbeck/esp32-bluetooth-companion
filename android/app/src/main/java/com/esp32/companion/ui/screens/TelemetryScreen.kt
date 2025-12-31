package com.esp32.companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esp32.companion.data.ConnectionState
import com.esp32.companion.viewmodel.MainViewModel

@Composable
fun TelemetryScreen(viewModel: MainViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (connectionState != ConnectionState.CONNECTED) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Not connected to a device")
            }
        } else {
            // Control buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.requestTelemetry(5000) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start Telemetry")
                }
                IconButton(onClick = { viewModel.requestStatus() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
            
            // Telemetry cards
            TelemetryCard(
                title = "System Information",
                items = listOf(
                    "Device" to "ESP32-DEV",
                    "Uptime" to "1h 23m",
                    "Version" to "1.0.0"
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TelemetryCard(
                title = "Memory",
                items = listOf(
                    "Free RAM" to "45 KB",
                    "Total RAM" to "320 KB",
                    "Usage" to "86%"
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TelemetryCard(
                title = "CPU",
                items = listOf(
                    "Frequency" to "160 MHz",
                    "Temperature" to "52°C"
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TelemetryCard(
                title = "Sensors",
                items = listOf(
                    "Temperature" to "25.5°C",
                    "Humidity" to "60.2%"
                )
            )
        }
    }
}

@Composable
fun TelemetryCard(
    title: String,
    items: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
