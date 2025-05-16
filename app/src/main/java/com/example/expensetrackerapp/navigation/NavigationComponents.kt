// 8. Create NavigationComponents.kt in com.example.expensetrackerapp.navigation package
package com.example.expensetrackerapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

// Define the main screens for bottom navigation
sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Categories : Screen("categories", "Categories")
    object Settings : Screen("settings", "Settings")
}

// Define detailed screens that need parameters
sealed class DetailScreen(val route: String) {
    object ExpenseEntry : DetailScreen("expense_entry/{categoryId}") {
        fun createRoute(categoryId: String) = "expense_entry/$categoryId"
    }
}

@Composable
fun ExpenseTrackerBottomNavigation(navController: NavController) {
    val items = listOf(
        NavigationItem(
            title = Screen.Home.title,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            route = Screen.Home.route
        ),
        NavigationItem(
            title = Screen.Categories.title,
            selectedIcon = Icons.Filled.List,
            unselectedIcon = Icons.Outlined.List,
            route = Screen.Categories.route
        ),
        NavigationItem(
            title = Screen.Settings.title,
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            route = Screen.Settings.route
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (currentRoute == item.route) {
                            item.selectedIcon
                        } else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)