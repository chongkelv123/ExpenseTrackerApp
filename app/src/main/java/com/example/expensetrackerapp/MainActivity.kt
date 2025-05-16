// 10. Update MainActivity.kt
package com.example.expensetrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.expensetrackerapp.ui.theme.ExpenseTrackerAppTheme
import com.example.expensetrackerapp.ui.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {
    private val expenseViewModel: ExpenseViewModel by viewModels {
        val app = application as ExpenseTrackerApplication
        ExpenseViewModel.Factory(
            app.expenseRepository,
            app.budgetRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge design
        enableEdgeToEdge()

        setContent {
            ExpenseTrackerAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the viewModel to the navigation to share it across screens
                    ExpenseTrackerNavigation(viewModel = expenseViewModel)
                }
            }
        }
    }
}