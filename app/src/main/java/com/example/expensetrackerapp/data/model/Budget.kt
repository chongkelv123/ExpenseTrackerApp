package com.example.expensetrackerapp.data.model

data class Budget(
    val category: ExpenseCategory,
    val amount: Double,
    val dateRange: String
)