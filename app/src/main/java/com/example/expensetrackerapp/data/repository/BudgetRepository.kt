// 6. Create BudgetRepository.kt in com.example.expensetrackerapp.data.repository package
package com.example.expensetrackerapp.data.repository

import com.example.expensetrackerapp.data.dao.BudgetDao
import com.example.expensetrackerapp.data.model.Budget
import com.example.expensetrackerapp.data.model.ExpenseCategory
import com.example.expensetrackerapp.data.model.MonthYear
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    fun getBudgetsByMonth(monthYear: MonthYear): Flow<List<Budget>> =
        budgetDao.getBudgetsByMonth(monthYear.toFormattedString())

    fun getBudgetForCategoryAndMonth(
        category: ExpenseCategory,
        monthYear: MonthYear
    ): Flow<Budget?> = budgetDao.getBudgetForCategoryAndMonth(
        category,
        monthYear.toFormattedString()
    )

    suspend fun insertBudget(budget: Budget) = budgetDao.insertBudget(budget)

    suspend fun updateBudget(budget: Budget) = budgetDao.updateBudget(budget)
}