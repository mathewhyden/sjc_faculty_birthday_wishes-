package com.example.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Directory : Screen("directory", "Staff Directory", Icons.Default.People)
    object Departments : Screen("departments", "Dept Aliases", Icons.Default.Domain)
    object Settings : Screen("settings", "Institution Info", Icons.Default.Info)

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(Dashboard, Directory, Departments, Settings)
    }
}
