package com.example.expensetrackerapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensetrackerapp.data.model.ExpenseCategory
import com.example.expensetrackerapp.ui.screens.categories.CategoryScreen
import com.example.expensetrackerapp.ui.screens.expense.ExpenseEntryScreen
import com.example.expensetrackerapp.ui.screens.home.HomeScreen
import com.example.expensetrackerapp.ui.screens.settings.SettingsScreen
import com.example.expensetrackerapp.ui.viewmodel.ExpenseViewModel

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
fun ExpenseTrackerNavigation(viewModel: ExpenseViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(navController)) {
                ExpenseTrackerBottomNavigation(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToCategory = { category ->
                        navController.navigate(
                            DetailScreen.ExpenseEntry.createRoute(category.name)
                        )
                    }
                )
            }

            composable(Screen.Categories.route) {
                CategoryScreen(
                    onNavigateToCategory = { category ->
                        navController.navigate(
                            DetailScreen.ExpenseEntry.createRoute(category.name)
                        )
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }

            composable(DetailScreen.ExpenseEntry.route) { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryId") ?: return@composable
                val category = try {
                    ExpenseCategory.valueOf(categoryName)
                } catch (e: IllegalArgumentException) {
                    ExpenseCategory.OTHERS
                }

                ExpenseEntryScreen(
                    category = category,
                    onAddExpense = { cat, amount, desc, date ->
                        viewModel.addExpense(cat, amount, desc, date)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun shouldShowBottomBar(navController: NavController): Boolean {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    return when {
        // These routes show the bottom bar
        currentRoute == Screen.Home.route -> true
        currentRoute == Screen.Categories.route -> true
        currentRoute == Screen.Settings.route -> true
        // These routes do not
        currentRoute?.startsWith(DetailScreen.ExpenseEntry.route.split("/")[0]) == true -> false
        else -> true
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
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
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