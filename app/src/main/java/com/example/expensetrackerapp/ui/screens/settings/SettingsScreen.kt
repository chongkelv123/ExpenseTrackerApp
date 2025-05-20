package com.example.expensetrackerapp.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetrackerapp.data.model.ExpenseCategory
import com.example.expensetrackerapp.ui.components.CustomDateRangeDialog
import com.example.expensetrackerapp.ui.theme.*
import com.example.expensetrackerapp.ui.viewmodel.ExpenseViewModel

@Composable
fun SettingsScreen(viewModel: ExpenseViewModel) {
    val rangeBudgets by viewModel.rangeBudgets.collectAsState()
    val currentDateRange by viewModel.currentDateRange.collectAsState()

    // State for custom date range dialog
    var showCustomRangeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Date Range selection card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Current Period",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentDateRange.toDisplayString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showCustomRangeDialog = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Date Range"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Customize Period")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Budget settings section
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Budget Settings for ${currentDateRange.toDisplayString()}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Budget settings for each category
                ExpenseCategory.values().forEach { category ->
                    val budget = rangeBudgets.find { it.category == category }?.amount ?: 0.0
                    BudgetSettingItem(
                        category = category,
                        currentBudget = budget,
                        onBudgetChange = { newBudget ->
                            viewModel.updateBudget(category, newBudget)
                        }
                    )
                    if (category != ExpenseCategory.values().last()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Theme settings card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Theme Settings",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Theme options would go here
                Text(
                    text = "Dynamic theme based on system settings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // App info
        Text(
            text = "ExpenseTracker v1.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

    // Custom date range dialog
    if (showCustomRangeDialog) {
        CustomDateRangeDialog(
            currentRange = currentDateRange,
            onDismiss = { showCustomRangeDialog = false },
            onConfirm = { newRange ->
                viewModel.setCurrentDateRange(newRange)
                showCustomRangeDialog = false
            }
        )
    }
}

@Composable
fun BudgetSettingItem(
    category: ExpenseCategory,
    currentBudget: Double,
    onBudgetChange: (Double) -> Unit
) {
    var budgetAmount by remember(currentBudget) {
        mutableStateOf(if (currentBudget > 0) currentBudget.toString() else "")
    }

    val categoryColor = when (category) {
        ExpenseCategory.NTUC -> categoryNtuc
        ExpenseCategory.MEAL -> categoryMeal
        ExpenseCategory.FUEL -> categoryFuel
        ExpenseCategory.JL_JE -> categoryJlJe
        ExpenseCategory.OTHERS -> categoryOthers
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = categoryColor,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "S$",
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = budgetAmount,
                onValueChange = {
                    // Only allow numbers and decimal point
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        budgetAmount = it
                    }
                },
                modifier = Modifier.width(120.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Budget"
                    )
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    val newBudget = budgetAmount.toDoubleOrNull() ?: 0.0
                    onBudgetChange(newBudget)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save Budget"
                )
            }
        }
    }
}