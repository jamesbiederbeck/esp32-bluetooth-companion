package com.esp32.companion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.esp32.companion.bluetooth.BluetoothConnectionManager
import com.esp32.companion.bluetooth.ProtocolHandler
import com.esp32.companion.data.BluetoothDeviceInfo
import com.esp32.companion.data.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Main ViewModel for the application
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val connectionManager = BluetoothConnectionManager(application)
    private val protocolHandler = ProtocolHandler(connectionManager)
    
    val connectionState: StateFlow<ConnectionState> = connectionManager.connectionState
    val connectedDevice: StateFlow<BluetoothDeviceInfo?> = connectionManager.connectedDevice
    val replOutput: StateFlow<String> = protocolHandler.replOutput
    val scannedDevices: StateFlow<List<BluetoothDeviceInfo>> = connectionManager.scannedDevices
    
    private val _availableDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val availableDevices: StateFlow<List<BluetoothDeviceInfo>> = _availableDevices.asStateFlow()
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    /**
     * Check if Bluetooth is available
     */
    fun isBluetoothAvailable(): Boolean {
        return connectionManager.isBluetoothAvailable()
    }
    
    /**
     * Check if Bluetooth is enabled
     */
    fun isBluetoothEnabled(): Boolean {
        return connectionManager.isBluetoothEnabled()
    }
    
    /**
     * Start BLE scan for devices
     */
    fun startBleScan() {
        viewModelScope.launch {
            _isScanning.value = true
            connectionManager.startScan()
            // Scan for 10 seconds
            kotlinx.coroutines.delay(10000)
            stopBleScan()
        }
    }
    
    /**
     * Stop BLE scan
     */
    fun stopBleScan() {
        connectionManager.stopScan()
        _isScanning.value = false
    }
    
    /**
     * Load paired devices
     */
    fun loadPairedDevices() {
        viewModelScope.launch {
            _isScanning.value = true
            val devices = connectionManager.getPairedDevices()
            _availableDevices.value = devices
            _isScanning.value = false
        }
    }
    
    /**
     * Connect to a device
     */
    fun connectToDevice(deviceAddress: String) {
        viewModelScope.launch {
            connectionManager.connect(deviceAddress)
        }
    }
    
    /**
     * Disconnect from current device
     */
    fun disconnect() {
        viewModelScope.launch {
            connectionManager.disconnect()
        }
    }
    
    /**
     * Send a REPL command
     */
    fun sendReplCommand(command: String) {
        viewModelScope.launch {
            protocolHandler.sendReplCommand(command)
        }
    }
    
    /**
     * Request file list
     */
    fun requestFileList(path: String = "/") {
        viewModelScope.launch {
            protocolHandler.requestFileList(path)
        }
    }
    
    /**
     * Request device status
     */
    fun requestStatus() {
        viewModelScope.launch {
            protocolHandler.requestStatus()
        }
    }
    
    /**
     * Send ping
     */
    fun sendPing() {
        viewModelScope.launch {
            protocolHandler.sendPing()
        }
    }
    
    /**
     * Request telemetry
     */
    fun requestTelemetry(intervalMs: Int = 5000) {
        viewModelScope.launch {
            protocolHandler.requestTelemetry(intervalMs)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            connectionManager.disconnect()
        }
    }
}
