// app/src/main/java/com/example/expensetrackerapp/data/repository/InMemoryExpenseRepository.kt
package com.example.expensetrackerapp.data.repository

import com.example.expensetrackerapp.data.model.Budget
import com.example.expensetrackerapp.data.model.Expense
import com.example.expensetrackerapp.data.model.ExpenseCategory
import com.example.expensetrackerapp.data.model.MonthYear
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong

class InMemoryExpenseRepository {
    // Counter for generating unique IDs
    private val idCounter = AtomicLong(1)

    // In-memory storage
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())

    // Public accessors
    val expenses: StateFlow<List<Expense>> = _expenses
    val budgets: StateFlow<List<Budget>> = _budgets

    // Expense operations
    fun getAllExpenses(): Flow<List<Expense>> = _expenses

    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>> {
        return _expenses.map { expenses -> expenses.filter { it.category == category } }
    }

    fun getExpensesByMonth(monthYear: MonthYear): Flow<List<Expense>> {
        return _expenses.map { expenses ->
            expenses.filter { expense ->
                val date = expense.date
                date.year == monthYear.year && date.monthValue == monthYear.month
            }
        }
    }

    suspend fun insertExpense(expense: Expense): Long {
        val id = idCounter.getAndIncrement()
        val newExpense = expense.copy(id = id)
        _expenses.value = _expenses.value + newExpense
        return id
    }

    suspend fun updateExpense(expense: Expense) {
        _expenses.value = _expenses.value.map {
            if (it.id == expense.id) expense else it
        }
    }

    suspend fun deleteExpense(expense: Expense) {
        _expenses.value = _expenses.value.filter { it.id != expense.id }
    }

    suspend fun getExpenseById(id: Long): Expense? {
        return _expenses.value.find { it.id == id }
    }

    // Budget operations
    fun getBudgetsByMonth(monthYear: MonthYear): Flow<List<Budget>> {
        return _budgets.map { budgets ->
            budgets.filter { it.monthYear == monthYear.toFormattedString() }
        }
    }

    fun getBudgetForCategoryAndMonth(
        category: ExpenseCategory,
        monthYear: MonthYear
    ): Flow<Budget?> {
        return _budgets.map { budgets ->
            budgets.find {
                it.category == category && it.monthYear == monthYear.toFormattedString()
            }
        }
    }

    suspend fun insertBudget(budget: Budget) {
        // First remove any existing budget for the same category and month
        _budgets.value = _budgets.value.filter {
            !(it.category == budget.category && it.monthYear == budget.monthYear)
        } + budget
    }
}