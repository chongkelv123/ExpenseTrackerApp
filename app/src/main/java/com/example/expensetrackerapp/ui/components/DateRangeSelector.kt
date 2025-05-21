package com.example.expensetrackerapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetrackerapp.data.model.DateRange
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter

enum class DateRangeType {
    BUDGET_CYCLE,
    CALENDAR_MONTH,
    CUSTOM
}

@Composable
fun DateRangeSelector(
    currentRange: DateRange,
    onPreviousRange: () -> Unit,
    onNextRange: () -> Unit,
    onCustomRangeClick: () -> Unit,
    onRangeTypeChange: (DateRangeType) -> Unit
) {
    var showOptionsMenu by remember { mutableStateOf(false) }
    val currentRangeType = remember(currentRange) {
        when {
            currentRange.isBudgetCycle() -> DateRangeType.BUDGET_CYCLE
            currentRange.isCalendarMonth() -> DateRangeType.CALENDAR_MONTH
            else -> DateRangeType.CUSTOM
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Period type indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentRange.getPeriodTypeName(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(onClick = { showOptionsMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Change Period Type"
                    )
                }

                // Dropdown menu for options
                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Budget Cycle (13th-12th)") },
                        onClick = {
                            onRangeTypeChange(DateRangeType.BUDGET_CYCLE)
                            showOptionsMenu = false
                        },
                        leadingIcon = {
                            RadioButton(
                                selected = currentRangeType == DateRangeType.BUDGET_CYCLE,
                                onClick = null
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Calendar Month") },
                        onClick = {
                            onRangeTypeChange(DateRangeType.CALENDAR_MONTH)
                            showOptionsMenu = false
                        },
                        leadingIcon = {
                            RadioButton(
                                selected = currentRangeType == DateRangeType.CALENDAR_MONTH,
                                onClick = null
                            )
                        }
                    )

                    Divider()

                    DropdownMenuItem(
                        text = { Text("Custom Date Range...") },
                        onClick = {
                            onCustomRangeClick()
                            showOptionsMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null
                            )
                        }
                    )
                }
            }

            // Main date display with navigation controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousRange) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Period"
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onCustomRangeClick() }
                ) {
                    Text(
                        text = currentRange.toDisplayString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onNextRange) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Period"
                    )
                }
            }
        }
    }
}