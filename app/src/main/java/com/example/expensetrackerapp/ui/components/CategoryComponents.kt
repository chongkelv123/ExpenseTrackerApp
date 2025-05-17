// 7. Create CategoryComponents.kt in com.example.expensetrackerapp.ui.components package
package com.example.expensetrackerapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetrackerapp.data.model.CategorySummary
import com.example.expensetrackerapp.data.model.Expense
import com.example.expensetrackerapp.data.model.ExpenseCategory
import java.text.NumberFormat
//import java.time.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import com.example.expensetrackerapp.data.model.MonthYear

// Category colors
val categoryNtuc = Color(0xFF2196F3) // Blue
val categoryMeal = Color(0xFF4CAF50) // Green
val categoryFuel = Color(0xFFF44336) // Red
val categoryJlJe = Color(0xFF9C27B0) // Purple
val categoryOthers = Color(0xFFFF9800) // Orange

fun getCategoryColor(category: ExpenseCategory): Color {
    return when (category) {
        ExpenseCategory.NTUC -> categoryNtuc
        ExpenseCategory.MEAL -> categoryMeal
        ExpenseCategory.FUEL -> categoryFuel
        ExpenseCategory.JL_JE -> categoryJlJe
        ExpenseCategory.OTHERS -> categoryOthers
    }
}

// Format currency in SGD
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "SG"))
    return format.format(amount)
}

@Composable
fun MonthYearSelector(
    currentMonth: MonthYear,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Month"
                )
            }

            Text(
                text = currentMonth.toDisplayString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Month"
                )
            }
        }
    }
}

@Composable
fun MonthlySummaryCard(
    totalSpent: Double,
    totalBudget: Double,
    percentage: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Monthly Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatCurrency(totalSpent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatCurrency(totalBudget),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = percentage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = when {
                    percentage < 0.7f -> MaterialTheme.colorScheme.primary
                    percentage < 0.9f -> Color(0xFFFFA000) // Amber
                    else -> Color(0xFFF44336) // Red
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Remaining: ${formatCurrency(totalBudget - totalSpent)}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (totalBudget >= totalSpent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color(0xFFF44336) // Red
                }
            )
        }
    }
}

@Composable
fun CategoryCard(
    categorySummary: CategorySummary,
    onAddExpense: () -> Unit
) {
    val categoryColor = getCategoryColor(categorySummary.category)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categorySummary.category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = categoryColor,
                    fontWeight = FontWeight.Bold
                )

                FilledTonalIconButton(
                    onClick = onAddExpense,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = categoryColor.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add ${categorySummary.category.displayName} Expense",
                        tint = categoryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = formatCurrency(categorySummary.spent),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = formatCurrency(categorySummary.budget),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = categorySummary.percentage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = categoryColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Remaining: ${formatCurrency(categorySummary.remainingBudget)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (categorySummary.remainingBudget >= 0) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    Color(0xFFF44336) // Red
                }
            )
        }
    }
}

@Composable
fun TransactionItem(expense: Expense) {
    val categoryColor = getCategoryColor(expense.category)
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = categoryColor,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${expense.category.displayName} • ${expense.date.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatCurrency(expense.amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}