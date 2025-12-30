package com.esp32.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompanionApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionApp() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("ESP32 Bluetooth Companion") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DeviceDiscoveryCard()
            ReplCard()
            FileTransferCard()
            TelemetryCard()
            NotificationCard()
        }
    }
}

@Composable
private fun DeviceDiscoveryCard() {
    val devices = remember { mutableStateListOf("ESP32-DevKit") }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Device Discovery", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            devices.forEach { device ->
                Text("• $device")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* TODO: trigger scan */ }) {
                Text("Scan for Devices")
            }
        }
    }
}

@Composable
private fun ReplCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("REPL", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Output: Ready for commands")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* TODO: send REPL command */ }) {
                Text("Send Command")
            }
        }
    }
}

@Composable
private fun FileTransferCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("File Transfer", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("No transfers active")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* TODO: upload file */ }) {
                Text("Upload")
            }
        }
    }
}

@Composable
private fun TelemetryCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Telemetry", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Temp: -- °C | Voltage: -- mV | RSSI: --")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* TODO: request telemetry */ }) {
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun NotificationCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Notifications", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("No notifications yet")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* TODO: clear notifications */ }) {
                Text("Clear")
            }
        }
    }
}
