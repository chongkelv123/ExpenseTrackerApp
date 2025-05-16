// 9. Create ExpenseEntryScreen.kt in com.example.expensetrackerapp.ui.screens.expense package
package com.example.expensetrackerapp.ui.screens.expense

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
import com.example.expensetrackerapp.data.model.ExpenseCategory
import com.example.expensetrackerapp.ui.components.getCategoryColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryScreen(
    category: ExpenseCategory,
    onAddExpense: (ExpenseCategory, Double, String, LocalDate) -> Unit,
    onNavigateBack: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Add ${category.displayName} Expense") },
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
                        onAddExpense(category, amount, description, selectedDate)
                        onNavigateBack()
                    }
                },
                icon = { Icon(Icons.Default.Save, contentDescription = "Save") },
                text = { Text("Save") },
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

                    Text(
                        text = "Date: ${selectedDate.format(dateFormatter)}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { /* Show date picker dialog */ },
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