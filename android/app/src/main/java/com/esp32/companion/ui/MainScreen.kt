package com.esp32.companion.ui

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.esp32.companion.ui.screens.DeviceListScreen
import com.esp32.companion.ui.screens.ReplScreen
import com.esp32.companion.ui.screens.FilesScreen
import com.esp32.companion.ui.screens.TelemetryScreen
import com.esp32.companion.viewmodel.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Devices : Screen("devices", "Devices", Icons.Default.Bluetooth)
    object Repl : Screen("repl", "REPL", Icons.Default.Terminal)
    object Files : Screen("files", "Files", Icons.Default.Folder)
    object Telemetry : Screen("telemetry", "Telemetry", Icons.Default.ShowChart)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    
    // Request Bluetooth permissions
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    
    val permissionsState = rememberMultiplePermissionsState(permissions)
    
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentRoute) {
                            Screen.Devices.route -> Screen.Devices.title
                            Screen.Repl.route -> Screen.Repl.title
                            Screen.Files.route -> Screen.Files.title
                            Screen.Telemetry.route -> Screen.Telemetry.title
                            else -> "ESP32 Companion"
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Screen.Devices.icon, contentDescription = Screen.Devices.title) },
                    label = { Text(Screen.Devices.title) },
                    selected = currentRoute == Screen.Devices.route,
                    onClick = { navController.navigate(Screen.Devices.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Screen.Repl.icon, contentDescription = Screen.Repl.title) },
                    label = { Text(Screen.Repl.title) },
                    selected = currentRoute == Screen.Repl.route,
                    onClick = { navController.navigate(Screen.Repl.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Screen.Files.icon, contentDescription = Screen.Files.title) },
                    label = { Text(Screen.Files.title) },
                    selected = currentRoute == Screen.Files.route,
                    onClick = { navController.navigate(Screen.Files.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Screen.Telemetry.icon, contentDescription = Screen.Telemetry.title) },
                    label = { Text(Screen.Telemetry.title) },
                    selected = currentRoute == Screen.Telemetry.route,
                    onClick = { navController.navigate(Screen.Telemetry.route) }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Devices.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Devices.route) {
                DeviceListScreen(viewModel)
            }
            composable(Screen.Repl.route) {
                ReplScreen(viewModel)
            }
            composable(Screen.Files.route) {
                FilesScreen(viewModel)
            }
            composable(Screen.Telemetry.route) {
                TelemetryScreen(viewModel)
            }
        }
    }
}
