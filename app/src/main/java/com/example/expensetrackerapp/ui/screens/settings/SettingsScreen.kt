// 13. Update SettingsScreen.kt to use ViewModel for budgets
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
import com.example.expensetrackerapp.ui.theme.*
import com.example.expensetrackerapp.ui.viewmodel.ExpenseViewModel

@Composable
fun SettingsScreen(viewModel: ExpenseViewModel) {
    val monthlyBudgets by viewModel.monthlyBudgets.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()

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
                        text = "Monthly Budget Settings for ${currentMonth.toDisplayString()}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Budget settings for each category
                ExpenseCategory.values().forEach { category ->
                    val budget = monthlyBudgets.find { it.category == category }?.amount ?: 0.0
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

        Spacer(modifier = Modifier.height(32.dp))

        // App info
        Text(
            text = "ExpenseTracker v1.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
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