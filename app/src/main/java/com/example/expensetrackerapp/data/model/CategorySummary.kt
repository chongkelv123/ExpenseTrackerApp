// 5. Create CategorySummary.kt in com.example.expensetrackerapp.data.model package
package com.example.expensetrackerapp.data.model

data class CategorySummary(
    val category: ExpenseCategory,
    val spent: Double,
    val budget: Double,
    val percentage: Float,  // between 0f and 1f
    val remainingBudget: Double
)