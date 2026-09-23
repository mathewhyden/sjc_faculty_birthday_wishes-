package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DepartmentManagerScreen
import com.example.ui.screens.DirectoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToDirectory = {
                    navController.navigate(Screen.Directory.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Directory.route) {
            DirectoryScreen(viewModel = viewModel)
        }

        composable(Screen.Departments.route) {
            DepartmentManagerScreen(viewModel = viewModel)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = viewModel)
        }
    }
}
