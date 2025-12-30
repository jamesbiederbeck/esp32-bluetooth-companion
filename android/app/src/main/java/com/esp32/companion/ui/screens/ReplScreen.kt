package com.esp32.companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esp32.companion.data.ConnectionState
import com.esp32.companion.viewmodel.MainViewModel

@Composable
fun ReplScreen(viewModel: MainViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    val replOutput by viewModel.replOutput.collectAsState()
    var commandInput by remember { mutableStateOf("") }
    var outputHistory by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    val listState = rememberLazyListState()
    
    // Update history when output changes
    LaunchedEffect(replOutput) {
        if (replOutput.isNotEmpty()) {
            outputHistory = outputHistory + ("" to replOutput)
            listState.animateScrollToItem(outputHistory.size)
        }
    }
    
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
            // Output area
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (outputHistory.isEmpty()) {
                        item {
                            Text(
                                "MicroPython REPL - Type Python commands below",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(outputHistory) { (cmd, output) ->
                            if (cmd.isNotEmpty()) {
                                Text(
                                    ">>> $cmd",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (output.isNotEmpty()) {
                                Text(output)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Input area
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    label = { Text("Python command") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = {
                        if (commandInput.isNotEmpty()) {
                            outputHistory = outputHistory + (commandInput to "")
                            viewModel.sendReplCommand(commandInput)
                            commandInput = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Quick command buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        commandInput = "import machine; machine.freq()"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("CPU Freq")
                }
                Button(
                    onClick = {
                        commandInput = "import gc; gc.mem_free()"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Free Mem")
                }
            }
        }
    }
}
