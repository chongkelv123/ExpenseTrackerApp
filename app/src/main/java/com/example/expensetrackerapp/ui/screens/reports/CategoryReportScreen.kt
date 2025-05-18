package com.example.expensetrackerapp.ui.screens.reports

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetrackerapp.data.model.Expense
import com.example.expensetrackerapp.data.model.ExpenseCategory
import com.example.expensetrackerapp.ui.components.MonthYearSelector
import com.example.expensetrackerapp.ui.components.TransactionItem
import com.example.expensetrackerapp.ui.components.formatCurrency
import com.example.expensetrackerapp.ui.components.getCategoryColor
import com.example.expensetrackerapp.ui.viewmodel.ExpenseViewModel
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun CategoryReportScreen(
    viewModel: ExpenseViewModel,
    onNavigateToTransactionDetail: (Long) -> Unit
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsState()

    // Group expenses by category
    val expensesByCategory = remember(monthlyExpenses) {
        monthlyExpenses.groupBy { it.category }
    }

    // Calculate total for each category
    val categoryTotals = remember(expensesByCategory) {
        expensesByCategory.mapValues { (_, expenses) ->
            expenses.sumOf { it.amount }
        }
    }

    // Total spending for the month
    val totalSpending = remember(categoryTotals) {
        categoryTotals.values.sum()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Spending by Category",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Month selector at the top
        MonthYearSelector(
            currentMonth = currentMonth,
            onPreviousMonth = { viewModel.previousMonth() },
            onNextMonth = { viewModel.nextMonth() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Total spending for the month
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Total Spending",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatCurrency(totalSpending),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (expensesByCategory.isEmpty()) {
            // Empty state
            EmptyReportState()
        } else {
            // List of categories with their expenses
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sort categories by amount (highest first)
                val sortedCategories = ExpenseCategory.values().sortedByDescending {
                    categoryTotals[it] ?: 0.0
                }

                items(sortedCategories) { category ->
                    val expenses = expensesByCategory[category] ?: emptyList()
                    val total = categoryTotals[category] ?: 0.0
                    if (expenses.isNotEmpty()) {
                        CategoryExpenseItem(
                            category = category,
                            expenses = expenses,
                            total = total,
                            totalSpending = totalSpending,
                            onNavigateToTransactionDetail = onNavigateToTransactionDetail
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryExpenseItem(
    category: ExpenseCategory,
    expenses: List<Expense>,
    total: Double,
    totalSpending: Double,
    onNavigateToTransactionDetail: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(if (expanded) 180f else 0f)
    val categoryColor = getCategoryColor(category)

    // Calculate percentage of total
    val percentage = if (totalSpending > 0) {
        (total / totalSpending) * 100
    } else 0.0

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        // Category header with total and expand button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category color indicator
            Spacer(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = categoryColor,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor
                )

                Text(
                    text = String.format("%.1f%%", percentage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatCurrency(total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${expenses.size} transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                modifier = Modifier.rotate(rotationState)
            )
        }

        // Progress indicator showing percentage of total
        LinearProgressIndicator(
            progress = (percentage / 100).toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = categoryColor
        )

        // Expense details (visible when expanded)
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Sort expenses by date (newest first)
                val sortedExpenses = expenses.sortedByDescending { it.date }

                sortedExpenses.forEach { expense ->
                    Surface(
                        modifier = Modifier.clickable { onNavigateToTransactionDetail(expense.id) }
                    ) {
                        TransactionItem(expense = expense)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyReportState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No expenses for this month",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Start adding expenses to see your spending by category",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}