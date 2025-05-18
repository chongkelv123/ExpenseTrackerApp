package com.example.expensetrackerapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapp.data.model.Budget
import com.example.expensetrackerapp.data.model.CategorySummary
import com.example.expensetrackerapp.data.model.Expense
import com.example.expensetrackerapp.data.model.ExpenseCategory
import com.example.expensetrackerapp.data.model.MonthYear
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

    // Current selected month
    private val _currentMonth = MutableStateFlow(MonthYear.current())
    val currentMonth: StateFlow<MonthYear> = _currentMonth

    // Expose all expenses to support viewing any transaction detail
    val allExpenses: StateFlow<List<Expense>> = repository.expenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Get expenses for the current month
    val monthlyExpenses: StateFlow<List<Expense>> =
        combine(currentMonth, repository.expenses) { month, allExpenses ->
            allExpenses.filter {
                val expenseMonth = MonthYear.fromLocalDate(it.date)
                expenseMonth.month == month.month && expenseMonth.year == month.year
            }.sortedByDescending { it.date } // Sort by date, most recent first
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Get budgets for the current month
    val monthlyBudgets: StateFlow<List<Budget>> =
        combine(currentMonth, repository.budgets) { month, budgets ->
            // If any category doesn't have a budget, create a default one
            val existingCategories = budgets
                .filter { it.monthYear == month.toFormattedString() }
                .map { it.category }

            val allBudgets = budgets
                .filter { it.monthYear == month.toFormattedString() }
                .toMutableList()

            ExpenseCategory.values().forEach { category ->
                if (category !in existingCategories) {
                    allBudgets.add(
                        Budget(
                            category = category,
                            amount = 0.0,
                            monthYear = month.toFormattedString()
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

    // Calculate monthly summary
    val monthlySummary: StateFlow<MonthlySummary> =
        combine(currentMonth, monthlyExpenses, monthlyBudgets) { month, expenses, budgets ->
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
                month = month,
                totalSpent = totalSpent,
                totalBudget = totalBudget,
                categorySummaries = categorySummaries
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MonthlySummary(
                month = MonthYear.current(),
                totalSpent = 0.0,
                totalBudget = 0.0,
                categorySummaries = emptyList()
            )
        )

    // Functions for changing the current month
    fun setCurrentMonth(monthYear: MonthYear) {
        _currentMonth.value = monthYear
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.next()
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.previous()
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
                monthYear = _currentMonth.value.toFormattedString()
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