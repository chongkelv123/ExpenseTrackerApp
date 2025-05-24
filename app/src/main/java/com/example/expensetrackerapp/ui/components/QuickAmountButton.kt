package com.example.expensetrackerapp.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Common UI components used across multiple screens
 */

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