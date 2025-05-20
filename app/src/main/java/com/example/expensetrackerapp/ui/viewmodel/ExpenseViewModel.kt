package com.example.expensetrackerapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapp.data.model.Budget
import com.example.expensetrackerapp.data.model.CategorySummary
import com.example.expensetrackerapp.data.model.DateRange
import com.example.expensetrackerapp.data.model.Expense
import com.example.expensetrackerapp.data.model.ExpenseCategory
import com.example.expensetrackerapp.data.model.MonthlySummary
import com.example.expensetrackerapp.data.db.SQLiteExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class ExpenseViewModel(
    private val repository: SQLiteExpenseRepository
) : ViewModel() {

    // Current selected date range (replaced MonthYear)
    private val _currentDateRange = MutableStateFlow(DateRange.customDefault())
    val currentDateRange: StateFlow<DateRange> = _currentDateRange

    // Expose all expenses to support viewing any transaction detail
    val allExpenses: StateFlow<List<Expense>> = repository.expenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Get expenses for the current date range
    val rangeExpenses: StateFlow<List<Expense>> =
        combine(currentDateRange, repository.expenses) { range, allExpenses ->
            allExpenses.filter { expense ->
                val expenseDate = expense.date
                (expenseDate.isEqual(range.startDate) || expenseDate.isAfter(range.startDate)) &&
                        (expenseDate.isEqual(range.endDate) || expenseDate.isBefore(range.endDate))
            }.sortedByDescending { it.date } // Sort by date, most recent first
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Get budgets for the current date range
    val rangeBudgets: StateFlow<List<Budget>> =
        combine(currentDateRange, repository.budgets) { range, budgets ->
            // If any category doesn't have a budget, create a default one
            val existingCategories = budgets
                .filter { it.dateRange == range.toFormattedString() }
                .map { it.category }

            val allBudgets = budgets
                .filter { it.dateRange == range.toFormattedString() }
                .toMutableList()

            ExpenseCategory.values().forEach { category ->
                if (category !in existingCategories) {
                    allBudgets.add(
                        Budget(
                            category = category,
                            amount = 0.0,
                            dateRange = range.toFormattedString()
                        )
                    )
                }
            }

            allBudgets
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Calculate range summary
    val rangeSummary: StateFlow<MonthlySummary> =
        combine(currentDateRange, rangeExpenses, rangeBudgets) { range, expenses, budgets ->
            // Calculate category summaries
            val categorySummaries = ExpenseCategory.values().map { category ->
                val categoryExpenses = expenses.filter { it.category == category }
                val totalSpent = categoryExpenses.sumOf { it.amount }
                val categoryBudget = budgets.find { it.category == category }?.amount ?: 0.0

                CategorySummary(
                    category = category,
                    spent = totalSpent,
                    budget = categoryBudget,
                    percentage = if (categoryBudget > 0) {
                        (totalSpent / categoryBudget).toFloat().coerceAtMost(1.0f)
                    } else 0f,
                    remainingBudget = categoryBudget - totalSpent
                )
            }

            // Calculate total spent and budget
            val totalSpent = categorySummaries.sumOf { it.spent }
            val totalBudget = categorySummaries.sumOf { it.budget }

            MonthlySummary(
                dateRange = range, // Using correct parameter name
                totalSpent = totalSpent,
                totalBudget = totalBudget,
                categorySummaries = categorySummaries
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MonthlySummary(
                dateRange = DateRange.customDefault(), // Using correct parameter name
                totalSpent = 0.0,
                totalBudget = 0.0,
                categorySummaries = emptyList()
            )
        )

    // Functions for changing the current date range
    fun setCurrentDateRange(dateRange: DateRange) {
        _currentDateRange.value = dateRange
    }

    fun nextRange() {
        _currentDateRange.value = _currentDateRange.value.next()
    }

    fun previousRange() {
        _currentDateRange.value = _currentDateRange.value.previous()
    }

    // Expense CRUD operations
    fun addExpense(
        category: ExpenseCategory,
        amount: Double,
        description: String,
        date: LocalDate = LocalDate.now()
    ) {
        viewModelScope.launch {
            val expense = Expense(
                category = category,
                amount = amount,
                description = description,
                date = date
            )
            repository.insertExpense(expense)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Function to get single expense by ID
    suspend fun getExpenseById(id: Long): Expense? {
        return repository.getExpenseById(id)
    }

    // Budget operations
    fun updateBudget(category: ExpenseCategory, amount: Double) {
        viewModelScope.launch {
            val budget = Budget(
                category = category,
                amount = amount,
                dateRange = _currentDateRange.value.toFormattedString()
            )
            repository.insertBudget(budget)
        }
    }

    /**
     * Factory class for creating ExpenseViewModel instances
     */
    class Factory(private val repository: SQLiteExpenseRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ExpenseViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}