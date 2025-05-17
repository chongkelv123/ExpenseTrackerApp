package com.example.expensetrackerapp.data.model

import java.time.LocalDate

data class Expense(
    val id: Long = 0,
    val category: ExpenseCategory,
    val amount: Double,
    val description: String,
    val date: LocalDate,
    val receiptUri: String? = null
)