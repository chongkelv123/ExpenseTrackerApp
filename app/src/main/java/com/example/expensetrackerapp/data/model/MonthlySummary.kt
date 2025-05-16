// 6. Create MonthlySummary.kt in com.example.expensetrackerapp.data.model package
package com.example.expensetrackerapp.data.model

data class MonthlySummary(
    val month: MonthYear,
    val totalSpent: Double,
    val totalBudget: Double,
    val categorySummaries: List<CategorySummary>
)