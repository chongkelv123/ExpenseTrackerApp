package com.example.expensetrackerapp.ui.screens.settings

import androidx.compose.animation.*
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
import com.example.expensetrackerapp.ui.components.DateRangeType
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
                    val budgetState = budgetAmounts[category]
                        ?: remember { mutableStateOf("") }

                    BudgetSettingItem(
                        category = category,
                        budgetAmount = budgetState.value,
                        onBudgetAmountChange = { budgetState.value = it },
                        onSave = {
                            val amount = budgetState.value.toDoubleOrNull() ?: 0.0
                            handleBudgetUpdate(category, amount)
                        },
                        showSuccess = saveSuccess[category] == true
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
    budgetAmount: String,
    onBudgetAmountChange: (String) -> Unit,
    onSave: () -> Unit,
    showSuccess: Boolean = false
) {
    val categoryColor = when (category) {
        ExpenseCategory.NTUC -> categoryNtuc
        ExpenseCategory.MEAL -> categoryMeal
        ExpenseCategory.FUEL -> categoryFuel
        ExpenseCategory.JL_JE -> categoryJlJe
        ExpenseCategory.OTHERS -> categoryOthers
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
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
                            onBudgetAmountChange(it)
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
                    onClick = onSave,
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

        // Show success message
        AnimatedVisibility(
            visible = showSuccess,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Budget updated",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}