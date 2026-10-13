package com.rcempire.empirelauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.*
import com.rcempire.empirelauncher.ui.screens.*
import com.rcempire.empirelauncher.ui.theme.Bg

private data class Tab(val route: String, val label: String, val icon: ImageVector)
private val tabs = listOf(
    Tab("home", "Home", Icons.Default.Home),
    Tab("instances", "Instances", Icons.Default.Folder),
    Tab("store", "Store", Icons.Default.Store),
    Tab("accounts", "Accounts", Icons.Default.Person),
    Tab("settings", "Settings", Icons.Default.Settings)
)

@Composable
fun EmpireNav() {
    val nav = rememberNavController()
    val current by nav.currentBackStackEntryAsState()
    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEach { t ->
                    NavigationBarItem(
                        selected = current?.destination?.route == t.route,
                        onClick = { nav.navigate(t.route) { popUpTo("home"); launchSingleTop = true } },
                        icon = { Icon(t.icon, t.label) }, label = { Text(t.label) }
                    )
                }
            }
        }
    ) { pad ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(pad).fillMaxSize().background(Bg)) {
            composable("home") { HomeScreen(onControls = { nav.navigate("controls") }, onLogs = { nav.navigate("logs") }) }
            composable("instances") { InstancesScreen() }
            composable("store") { StoreScreen() }
            composable("accounts") { AccountsScreen() }
            composable("settings") { SettingsScreen(onLogs = { nav.navigate("logs") }, onControls = { nav.navigate("controls") }) }
            composable("controls") { ControlsScreen() }
            composable("logs") { LogsScreen() }
        }
    }
}
