package com.example.expensetrackerapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetrackerapp.data.model.DateRange
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangeDialog(
    currentRange: DateRange,
    onDismiss: () -> Unit,
    onConfirm: (DateRange) -> Unit
) {
    var startDate by remember { mutableStateOf(currentRange.startDate) }
    var endDate by remember { mutableStateOf(currentRange.endDate) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Date Range") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Select the start and end dates for your period")

                Spacer(modifier = Modifier.height(16.dp))

                // Start date selector
                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("Start Date", style = MaterialTheme.typography.bodySmall)
                        Text(
                            startDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // End date selector
                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("End Date", style = MaterialTheme.typography.bodySmall)
                        Text(
                            endDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Validation warning
                if (endDate.isBefore(startDate)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "End date must be after start date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!endDate.isBefore(startDate)) {
                        onConfirm(DateRange(startDate, endDate))
                    }
                },
                enabled = !endDate.isBefore(startDate)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Fixed date picker dialogs - properly updating the state
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = TimeUnit.DAYS.toMillis(startDate.toEpochDay())
        )

        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val epochDay = TimeUnit.MILLISECONDS.toDays(millis)
                        startDate = LocalDate.ofEpochDay(epochDay)
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = TimeUnit.DAYS.toMillis(endDate.toEpochDay())
        )

        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val epochDay = TimeUnit.MILLISECONDS.toDays(millis)
                        endDate = LocalDate.ofEpochDay(epochDay)
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}