package com.example.expensetrackerapp.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetrackerapp.data.model.DateRange
import com.example.expensetrackerapp.data.model.Expense
import com.example.expensetrackerapp.data.model.ExpenseCategory
import com.example.expensetrackerapp.ui.components.CategoryCard
import com.example.expensetrackerapp.ui.components.DateRangeSelector
import com.example.expensetrackerapp.ui.components.DateRangeType
import com.example.expensetrackerapp.ui.components.CustomDateRangeDialog
import com.example.expensetrackerapp.ui.components.MonthlySummaryCard
import com.example.expensetrackerapp.ui.components.TransactionItem
import com.example.expensetrackerapp.ui.viewmodel.ExpenseViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    onNavigateToCategory: (ExpenseCategory) -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit
) {
    val currentDateRange by viewModel.currentDateRange.collectAsState()
    val rangeSummary by viewModel.rangeSummary.collectAsState()
    val rangeExpenses by viewModel.rangeExpenses.collectAsState()
    val currentRangeType by viewModel.currentRangeType.collectAsState()

    // State for custom date range dialog
    var showCustomRangeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Enhanced Date Range Selector
        DateRangeSelector(
            currentRange = currentDateRange,
            onPreviousRange = { viewModel.previousRange() },
            onNextRange = { viewModel.nextRange() },
            onCustomRangeClick = { showCustomRangeDialog = true },
            onRangeTypeChange = { newType -> viewModel.setDateRangeType(newType) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Period spending summary
        MonthlySummaryCard(
            totalSpent = rangeSummary.totalSpent,
            totalBudget = rangeSummary.totalBudget,
            percentage = if (rangeSummary.totalBudget > 0) {
                (rangeSummary.totalSpent / rangeSummary.totalBudget).toFloat().coerceAtMost(1.0f)
            } else 0f
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category cards
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rangeSummary.categorySummaries) { categorySummary ->
                CategoryCard(
                    categorySummary = categorySummary,
                    onAddExpense = { onNavigateToCategory(categorySummary.category) }
                )
            }

            // Recent transactions section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Show recent transactions or a message if none exist
            if (rangeExpenses.isNotEmpty()) {
                items(rangeExpenses.take(5)) { expense ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTransactionDetail(expense.id) }
                    ) {
                        TransactionItem(expense = expense)
                    }
                }
            } else {
                item {
                    EmptyTransactionsMessage(currentDateRange)
                }
            }
        }
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
fun EmptyTransactionsMessage(dateRange: DateRange) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No expenses for ${dateRange.toDisplayString()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add your first expense by selecting a category",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}