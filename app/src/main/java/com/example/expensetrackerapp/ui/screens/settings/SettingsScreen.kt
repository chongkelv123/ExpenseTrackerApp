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
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapp.data.model.ExpenseCategory
import com.example.expensetrackerapp.ui.components.CustomDateRangeDialog
import com.example.expensetrackerapp.ui.components.DateRangeSelector
import com.example.expensetrackerapp.ui.theme.*
import com.example.expensetrackerapp.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: ExpenseViewModel) {
    val rangeBudgets by viewModel.rangeBudgets.collectAsState()
    val currentDateRange by viewModel.currentDateRange.collectAsState()
    val currentRangeType by viewModel.currentRangeType.collectAsState()

    // State for budget amounts
    val budgetAmounts = remember(rangeBudgets) {
        rangeBudgets.associate { budget ->
            budget.category to mutableStateOf(
                if (budget.amount > 0) budget.amount.toString() else ""
            )
        }
    }

    // State for custom date range dialog
    var showCustomRangeDialog by remember { mutableStateOf(false) }

    // State for success messages
    val saveSuccess = remember { mutableStateMapOf<ExpenseCategory, Boolean>() }

    // Handler for budget updates
    val handleBudgetUpdate = { category: ExpenseCategory, amount: Double ->
        viewModel.updateBudget(category, amount)

        // Show success message briefly for this category
        saveSuccess[category] = true

        // Set up a coroutine to hide the success message after delay
        viewModel.viewModelScope.launch {
            delay(2000)
            saveSuccess[category] = false
        }
    }

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

        // Date Range selection card with enhanced DateRangeSelector
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Period Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Use our enhanced DateRangeSelector
                DateRangeSelector(
                    currentRange = currentDateRange,
                    onPreviousRange = { viewModel.previousRange() },
                    onNextRange = { viewModel.nextRange() },
                    onCustomRangeClick = { showCustomRangeDialog = true },
                    onRangeTypeChange = { newType -> viewModel.setDateRangeType(newType) }
                )
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
                    BudgetSettingItem(
                        category = category,
                        viewModel = viewModel,
                        onSave = TODO(),
                        budgetAmount = TODO(),
                        onBudgetAmountChange = TODO(),
                        showSuccess = TODO()
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
    viewModel: ExpenseViewModel,
    onSave: () -> Unit,
    budgetAmount: String,
    onBudgetAmountChange: () -> Unit,
    showSuccess: Boolean
) {
    val categoryColor = when (category) {
        ExpenseCategory.NTUC -> categoryNtuc
        ExpenseCategory.MEAL -> categoryMeal
        ExpenseCategory.FUEL -> categoryFuel
        ExpenseCategory.JL_JE -> categoryJlJe
        ExpenseCategory.OTHERS -> categoryOthers
    }

    // Find the current budget from the viewModel
    val rangeBudgets by viewModel.rangeBudgets.collectAsState()
    val currentDateRange by viewModel.currentDateRange.collectAsState()

    // Find current budget amount
    val currentBudget = rangeBudgets.find { it.category == category }

    // State for editing
    var budgetAmount by remember(currentBudget) {
        mutableStateOf(
            if (currentBudget?.amount ?: 0.0 > 0)
                (currentBudget?.amount ?: 0.0).toString()
            else ""
        )
    }

    // Success state
    var showSuccess by remember { mutableStateOf(false) }

    // Launch effect to reset success after delay
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            delay(2000)
            showSuccess = false
        }
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

            Button(
                onClick = {
                    // Convert to double, default to 0.0 if empty
                    val amount = budgetAmount.toDoubleOrNull() ?: 0.0

                    // Use our fixed update method
                    viewModel.fixedUpdateBudget(category, amount)

                    // Show success
                    showSuccess = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = categoryColor
                )
            ) {
                if (!showSuccess) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Budget"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save")
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Saved"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Saved!")
                }
            }
        }
    }
}
