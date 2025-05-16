// 5. Create ExpenseRepository.kt in com.example.expensetrackerapp.data.repository package
package com.example.expensetrackerapp.data.repository

import com.example.expensetrackerapp.data.dao.ExpenseDao
import com.example.expensetrackerapp.data.model.Expense
import com.example.expensetrackerapp.data.model.ExpenseCategory
import com.example.expensetrackerapp.data.model.MonthYear
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>> =
        expenseDao.getExpensesByCategory(category)

    fun getExpensesByMonth(monthYear: MonthYear): Flow<List<Expense>> {
        val startDate = LocalDate.of(monthYear.year, monthYear.month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        return expenseDao.getExpensesByDateRange(startDate, endDate)
    }

    suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    suspend fun getExpenseById(id: Long): Expense? = expenseDao.getExpenseById(id)
}