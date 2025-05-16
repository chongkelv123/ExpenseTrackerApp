// 3. Create Budget.kt in com.example.expensetrackerapp.data.model package
package com.example.expensetrackerapp.data.model

import androidx.room.Entity

@Entity(tableName = "budgets", primaryKeys = ["category", "monthYear"])
data class Budget(
    val category: ExpenseCategory,
    val amount: Double,
    val monthYear: String
)