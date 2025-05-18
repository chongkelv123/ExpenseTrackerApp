package com.example.expensetrackerapp.ui.screens.transaction

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetrackerapp.data.model.Expense
import com.example.expensetrackerapp.data.model.ExpenseCategory
import com.example.expensetrackerapp.ui.components.getCategoryColor
import com.example.expensetrackerapp.ui.viewmodel.ExpenseViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditScreen(
    expenseId: Long,
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    // Find the expense in the current monthly expenses
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsState()
    val expense = remember(monthlyExpenses, expenseId) {
        monthlyExpenses.find { it.id == expenseId }
    }

    // If expense is null, show error state
    if (expense == null) {
        TransactionNotFoundScreen(onNavigateBack)
        return
    }

    // State for form fields
    var category by remember { mutableStateOf(expense.category) }
    var amountText by remember { mutableStateOf(expense.amount.toString()) }
    var description by remember { mutableStateOf(expense.description) }
    var selectedDate by remember { mutableStateOf(expense.date) }

    // State for date picker dialog
    var showDatePicker by remember { mutableStateOf(false) }

    // Format for displaying date
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Edit Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = getCategoryColor(category).copy(alpha = 0.1f)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        val updatedExpense = expense.copy(
                            category = category,
                            amount = amount,
                            description = description,
                            date = selectedDate
                        )
                        viewModel.updateExpense(updatedExpense)
                        onNavigateBack()
                    }
                },
                icon = { Icon(Icons.Default.Save, contentDescription = "Save") },
                text = { Text("Save Changes") },
                containerColor = getCategoryColor(category)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Category selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExpenseCategory.values().forEach { cat ->
                            val categoryColor = getCategoryColor(cat)
                            val isSelected = cat == category

                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat },
                                label = { Text(cat.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = categoryColor.copy(alpha = 0.2f),
                                    selectedLabelColor = categoryColor
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = getCategoryColor(category)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Transaction Date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = selectedDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        // Show "Today" or "Yesterday" or "X days ago" for context
                        val daysAgo = ChronoUnit.DAYS.between(selectedDate, LocalDate.now())
                        if (daysAgo > 0) {
                            Text(
                                text = when (daysAgo) {
                                    0L -> ""
                                    1L -> "Yesterday"
                                    else -> "$daysAgo days ago"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { showDatePicker = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = getCategoryColor(category)
                        )
                    ) {
                        Text("Change")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount input
            OutlinedTextField(
                value = amountText,
                onValueChange = { newValue ->
                    // Only allow numeric input with decimal point
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        amountText = newValue
                    }
                },
                label = { Text("Amount (S$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick amount buttons
            Text(
                text = "Quick Amount",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickAmountButton(amount = "5", onAmountSelected = { amountText = it })
                QuickAmountButton(amount = "10", onAmountSelected = { amountText = it })
                QuickAmountButton(amount = "20", onAmountSelected = { amountText = it })
                QuickAmountButton(amount = "50", onAmountSelected = { amountText = it })
                QuickAmountButton(amount = "100", onAmountSelected = { amountText = it })
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = TimeUnit.DAYS.toMillis(selectedDate.toEpochDay())
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Convert milliseconds to epoch days and then to LocalDate
                        val epochDay = TimeUnit.MILLISECONDS.toDays(millis)
                        selectedDate = LocalDate.ofEpochDay(epochDay)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                dateValidator = { timestamp ->
                    // Only allow dates up to today (not in the future)
                    timestamp <= TimeUnit.DAYS.toMillis(LocalDate.now().toEpochDay())
                },
                title = { Text("Select Date") }
            )
        }
    }
}

@Composable
fun TransactionNotFoundScreen(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Transaction not found",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNavigateBack) {
            Text("Go Back")
        }
    }
}

@Composable
fun QuickAmountButton(
    amount: String,
    onAmountSelected: (String) -> Unit
) {
    OutlinedButton(
        onClick = { onAmountSelected(amount) },
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text("S$$amount")
    }
}