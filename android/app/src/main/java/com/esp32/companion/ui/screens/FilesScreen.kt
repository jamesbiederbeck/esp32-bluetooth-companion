package com.esp32.companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esp32.companion.data.ConnectionState
import com.esp32.companion.viewmodel.MainViewModel

@Composable
fun FilesScreen(viewModel: MainViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    var currentPath by remember { mutableStateOf("/") }
    
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
            // Current path display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Path: $currentPath",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { viewModel.requestFileList(currentPath) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            }
            
            // File list (placeholder - would need file list state in ViewModel)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            "File browser - Implementation requires file list state in ViewModel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Example file items
                    item {
                        FileItem(
                            name = "boot.py",
                            isDirectory = false,
                            onClick = { /* Handle file click */ }
                        )
                    }
                    item {
                        FileItem(
                            name = "main.py",
                            isDirectory = false,
                            onClick = { /* Handle file click */ }
                        )
                    }
                    item {
                        FileItem(
                            name = "lib",
                            isDirectory = true,
                            onClick = { currentPath = "$currentPath/lib" }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* Upload file */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Upload")
                }
                Button(
                    onClick = { /* Create directory */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("New Dir")
                }
            }
        }
    }
}

@Composable
fun FileItem(
    name: String,
    isDirectory: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = if (isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(name, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
