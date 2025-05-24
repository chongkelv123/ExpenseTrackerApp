package com.example.expensetrackerapp.ui.viewmodel

import android.util.Log
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
import com.example.expensetrackerapp.ui.components.DateRangeType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

class ExpenseViewModel(
    private val repository: SQLiteExpenseRepository
) : ViewModel() {
    private val TAG = "ExpenseViewModel"

    // Current selected date range type
    private val _currentRangeType = MutableStateFlow(DateRangeType.BUDGET_CYCLE)
    val currentRangeType: StateFlow<DateRangeType> = _currentRangeType

    // Current selected date range (default to budget cycle)
    private val _currentDateRange = MutableStateFlow(DateRange.customDefault())
    val currentDateRange: StateFlow<DateRange> = _currentDateRange

    // Expose all expenses to support viewing any transaction detail
    val allExpenses: StateFlow<List<Expense>> = repository.expenses
        .catch { e ->
            Log.e(TAG, "Error in allExpenses flow: ${e.message}", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Get expenses for the current date range
    val rangeExpenses: StateFlow<List<Expense>> =
        combine(currentDateRange, repository.expenses) { range, allExpenses ->
            try {
                allExpenses.filter { expense ->
                    val expenseDate = expense.date
                    (expenseDate.isEqual(range.startDate) || expenseDate.isAfter(range.startDate)) &&
                            (expenseDate.isEqual(range.endDate) || expenseDate.isBefore(range.endDate))
                }.sortedByDescending { it.date }
            } catch (e: Exception) {
                Log.e(TAG, "Error filtering expenses by range: ${e.message}", e)
                emptyList()
            }
        }
            .catch { e ->
                Log.e(TAG, "Error in rangeExpenses flow: ${e.message}", e)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Get budgets for the current date range
    val rangeBudgets: StateFlow<List<Budget>> =
        combine(currentDateRange, repository.budgets) { range, budgets ->
            try {
                val rangeString = range.toFormattedString()
                val existingBudgets = budgets.filter { it.dateRange == rangeString }
                val existingCategories = existingBudgets.map { it.category }.toSet()

                // Create default budgets for missing categories
                val allBudgets = existingBudgets.toMutableList()
                ExpenseCategory.values().forEach { category ->
                    if (category !in existingCategories) {
                        allBudgets.add(
                            Budget(
                                category = category,
                                amount = 0.0,
                                dateRange = rangeString
                            )
                        )
                    }
                }

                allBudgets.sortedBy { it.category.ordinal }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting budgets for range: ${e.message}", e)
                // Return default empty budgets for each category
                ExpenseCategory.values().map { category ->
                    Budget(
                        category = category,
                        amount = 0.0,
                        dateRange = range.toFormattedString()
                    )
                }
            }
        }
            .catch { e ->
                Log.e(TAG, "Error in rangeBudgets flow: ${e.message}", e)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Calculate range summary
    val rangeSummary: StateFlow<MonthlySummary> =
        combine(currentDateRange, rangeExpenses, rangeBudgets) { range, expenses, budgets ->
            try {
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
                    dateRange = range,
                    totalSpent = totalSpent,
                    totalBudget = totalBudget,
                    categorySummaries = categorySummaries
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating range summary: ${e.message}", e)
                MonthlySummary(
                    dateRange = range,
                    totalSpent = 0.0,
                    totalBudget = 0.0,
                    categorySummaries = emptyList()
                )
            }
        }
            .catch { e ->
                Log.e(TAG, "Error in rangeSummary flow: ${e.message}", e)
                emit(
                    MonthlySummary(
                        dateRange = _currentDateRange.value,
                        totalSpent = 0.0,
                        totalBudget = 0.0,
                        categorySummaries = emptyList()
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = MonthlySummary(
                    dateRange = DateRange.customDefault(),
                    totalSpent = 0.0,
                    totalBudget = 0.0,
                    categorySummaries = emptyList()
                )
            )

    // Set date range type and update current range accordingly
    fun setDateRangeType(type: DateRangeType) {
        viewModelScope.launch {
            try {
                _currentRangeType.value = type

                // Update the current date range based on the selected type
                val newRange = when (type) {
                    DateRangeType.BUDGET_CYCLE -> DateRange.customDefault()
                    DateRangeType.CALENDAR_MONTH -> DateRange.currentCalendarMonth()
                    DateRangeType.CUSTOM -> _currentDateRange.value // Keep current if custom
                }

                setCurrentDateRange(newRange)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting date range type: ${e.message}", e)
            }
        }
    }

    // Functions for changing the current date range
    fun setCurrentDateRange(dateRange: DateRange) {
        viewModelScope.launch {
            try {
                _currentDateRange.value = dateRange

                // Update the range type based on the range characteristics
                _currentRangeType.value = when {
                    dateRange.isBudgetCycle() -> DateRangeType.BUDGET_CYCLE
                    dateRange.isCalendarMonth() -> DateRangeType.CALENDAR_MONTH
                    else -> DateRangeType.CUSTOM
                }

                // Ensure budgets exist for this range
                ensureBudgetsExistForRange(dateRange)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting current date range: ${e.message}", e)
            }
        }
    }

    private suspend fun ensureBudgetsExistForRange(dateRange: DateRange) {
        try {
            val rangeString = dateRange.toFormattedString()
            val currentBudgets = repository.getBudgetsForRange(dateRange)
            val existingCategories = currentBudgets.map { it.category }.toSet()

            // Create default budgets for any missing categories
            ExpenseCategory.values().forEach { category ->
                if (category !in existingCategories) {
                    val budget = Budget(
                        category = category,
                        amount = 0.0,
                        dateRange = rangeString
                    )
                    repository.insertBudget(budget)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring budgets exist: ${e.message}", e)
        }
    }

    fun nextRange() {
        viewModelScope.launch {
            try {
                val nextRange = when (_currentRangeType.value) {
                    DateRangeType.BUDGET_CYCLE -> _currentDateRange.value.nextBudgetCycle()
                    DateRangeType.CALENDAR_MONTH -> {
                        val currentYearMonth = YearMonth.from(_currentDateRange.value.startDate)
                        DateRange.forCalendarMonth(currentYearMonth.plusMonths(1))
                    }
                    DateRangeType.CUSTOM -> _currentDateRange.value.next()
                }
                setCurrentDateRange(nextRange)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting next range: ${e.message}", e)
            }
        }
    }

    fun previousRange() {
        viewModelScope.launch {
            try {
                val prevRange = when (_currentRangeType.value) {
                    DateRangeType.BUDGET_CYCLE -> _currentDateRange.value.previousBudgetCycle()
                    DateRangeType.CALENDAR_MONTH -> {
                        val currentYearMonth = YearMonth.from(_currentDateRange.value.startDate)
                        DateRange.forCalendarMonth(currentYearMonth.minusMonths(1))
                    }
                    DateRangeType.CUSTOM -> _currentDateRange.value.previous()
                }
                setCurrentDateRange(prevRange)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting previous range: ${e.message}", e)
            }
        }
    }

    // Expense CRUD operations
    fun addExpense(
        category: ExpenseCategory,
        amount: Double,
        description: String,
        date: LocalDate = LocalDate.now()
    ) {
        viewModelScope.launch {
            try {
                val expense = Expense(
                    category = category,
                    amount = amount,
                    description = description,
                    date = date
                )
                repository.insertExpense(expense)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding expense: ${e.message}", e)
            }
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.updateExpense(expense)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating expense: ${e.message}", e)
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expense)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting expense: ${e.message}", e)
            }
        }
    }

    // Function to get single expense by ID
    suspend fun getExpenseById(id: Long): Expense? {
        return try {
            repository.getExpenseById(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting expense by ID: ${e.message}", e)
            null
        }
    }

    // Simplified budget update operation
    fun updateBudget(category: ExpenseCategory, amount: Double) {
        viewModelScope.launch {
            try {
                val budget = Budget(
                    category = category,
                    amount = amount,
                    dateRange = _currentDateRange.value.toFormattedString()
                )

                // Save the budget
                repository.insertBudget(budget)

                // Force refresh to ensure UI updates
                repository.refreshBudgets()

                // Small trick to force UI recomposition if needed
                val currentRange = _currentDateRange.value
                _currentDateRange.value = currentRange

            } catch (e: Exception) {
                Log.e(TAG, "Error updating budget: ${e.message}", e)
            }
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