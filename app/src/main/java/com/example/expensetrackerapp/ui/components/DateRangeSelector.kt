package com.example.expensetrackerapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetrackerapp.data.model.DateRange

@Composable
fun DateRangeSelector(
    currentRange: DateRange,
    onPreviousRange: () -> Unit,
    onNextRange: () -> Unit,
    onCustomRangeClick: () -> Unit
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
            IconButton(onClick = onPreviousRange) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Period"
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentRange.toDisplayString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Button to customize the range
                TextButton(onClick = onCustomRangeClick) {
                    Text("Customize", style = MaterialTheme.typography.bodySmall)
                }
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