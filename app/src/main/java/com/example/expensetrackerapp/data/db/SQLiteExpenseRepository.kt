package com.example.expensetrackerapp.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.expensetrackerapp.data.model.Budget
import com.example.expensetrackerapp.data.model.DateRange
import com.example.expensetrackerapp.data.model.Expense
import com.example.expensetrackerapp.data.model.ExpenseCategory
import com.example.expensetrackerapp.data.model.MonthYear
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate

/**
 * Repository implementation using SQLite directly.
 * Handles data access operations for expenses and budgets.
 */
class SQLiteExpenseRepository(context: Context) {
    private val dbHelper = ExpenseDbHelper(context)
    private val ioDispatcher = Dispatchers.IO.limitedParallelism(1) // Single thread for DB operations
    private val scope = MainScope() // Scope for repository-level coroutines


    // StateFlows to provide reactive data access
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets

    init {
        // Load initial data without blocking initialization
        scope.launch {
            refreshExpenses()
            refreshBudgets()
        }
    }

    // Refreshes the expenses StateFlow with current database data
    private suspend fun refreshExpenses() = withContext(ioDispatcher) {
        val expenses = getAllExpensesFromDb()
        _expenses.update { expenses }
    }

    // Refreshes the budgets StateFlow with current database data
    private suspend fun refreshBudgets() = withContext(ioDispatcher) {
        val budgets = getAllBudgetsFromDb()
        _budgets.update { budgets }
    }

    // EXPENSE OPERATIONS

    suspend fun insertExpense(expense: Expense): Long = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(ExpenseDbHelper.COLUMN_CATEGORY, expense.category.name)
            put(ExpenseDbHelper.COLUMN_AMOUNT, expense.amount)
            put(ExpenseDbHelper.COLUMN_DESCRIPTION, expense.description)
            put(ExpenseDbHelper.COLUMN_DATE, expense.date.toString())
            expense.receiptUri?.let { put(ExpenseDbHelper.COLUMN_RECEIPT_URI, it) }
        }

        val newId = db.insert(ExpenseDbHelper.TABLE_EXPENSES, null, values)
        refreshExpenses()
        newId
    }

    suspend fun updateExpense(expense: Expense) = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(ExpenseDbHelper.COLUMN_CATEGORY, expense.category.name)
            put(ExpenseDbHelper.COLUMN_AMOUNT, expense.amount)
            put(ExpenseDbHelper.COLUMN_DESCRIPTION, expense.description)
            put(ExpenseDbHelper.COLUMN_DATE, expense.date.toString())
            expense.receiptUri?.let { put(ExpenseDbHelper.COLUMN_RECEIPT_URI, it) }
        }

        val selection = "${ExpenseDbHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(expense.id.toString())

        db.update(
            ExpenseDbHelper.TABLE_EXPENSES,
            values,
            selection,
            selectionArgs
        )

        refreshExpenses()
    }

    suspend fun deleteExpense(expense: Expense) = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase

        val selection = "${ExpenseDbHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(expense.id.toString())

        db.delete(ExpenseDbHelper.TABLE_EXPENSES, selection, selectionArgs)

        refreshExpenses()
    }

    suspend fun getExpenseById(id: Long): Expense? = withContext(ioDispatcher) {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            ExpenseDbHelper.COLUMN_ID,
            ExpenseDbHelper.COLUMN_CATEGORY,
            ExpenseDbHelper.COLUMN_AMOUNT,
            ExpenseDbHelper.COLUMN_DESCRIPTION,
            ExpenseDbHelper.COLUMN_DATE,
            ExpenseDbHelper.COLUMN_RECEIPT_URI
        )

        val selection = "${ExpenseDbHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        val cursor = db.query(
            ExpenseDbHelper.TABLE_EXPENSES,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var expense: Expense? = null

        if (cursor.moveToFirst()) {
            expense = cursorToExpense(cursor)
        }

        cursor.close()

        expense
    }

    private suspend fun getAllExpensesFromDb(): List<Expense> = withContext(ioDispatcher) {
        val expenses = mutableListOf<Expense>()
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            ExpenseDbHelper.COLUMN_ID,
            ExpenseDbHelper.COLUMN_CATEGORY,
            ExpenseDbHelper.COLUMN_AMOUNT,
            ExpenseDbHelper.COLUMN_DESCRIPTION,
            ExpenseDbHelper.COLUMN_DATE,
            ExpenseDbHelper.COLUMN_RECEIPT_URI
        )

        val sortOrder = "${ExpenseDbHelper.COLUMN_DATE} DESC"

        val cursor = db.query(
            ExpenseDbHelper.TABLE_EXPENSES,
            projection,
            null,
            null,
            null,
            null,
            sortOrder
        )

        while (cursor.moveToNext()) {
            expenses.add(cursorToExpense(cursor))
        }

        cursor.close()

        expenses
    }

    private fun cursorToExpense(cursor: Cursor): Expense {
        val idIndex = cursor.getColumnIndexOrThrow(ExpenseDbHelper.COLUMN_ID)
        val categoryIndex = cursor.getColumnIndexOrThrow(ExpenseDbHelper.COLUMN_CATEGORY)
        val amountIndex = cursor.getColumnIndexOrThrow(ExpenseDbHelper.COLUMN_AMOUNT)
        val descriptionIndex = cursor.getColumnIndexOrThrow(ExpenseDbHelper.COLUMN_DESCRIPTION)
        val dateIndex = cursor.getColumnIndexOrThrow(ExpenseDbHelper.COLUMN_DATE)
        val receiptUriIndex = cursor.getColumnIndexOrThrow(ExpenseDbHelper.COLUMN_RECEIPT_URI)

        return Expense(
            id = cursor.getLong(idIndex),
            category = try {
                ExpenseCategory.valueOf(cursor.getString(categoryIndex))
            } catch (e: Exception) {
                ExpenseCategory.OTHERS // Fallback if category can't be parsed
            },
            amount = cursor.getDouble(amountIndex),
            description = cursor.getString(descriptionIndex),
            date = try {
                LocalDate.parse(cursor.getString(dateIndex))
            } catch (e: Exception) {
                LocalDate.now() // Fallback if date can't be parsed
            },
            receiptUri = if (cursor.isNull(receiptUriIndex)) null else cursor.getString(receiptUriIndex)
        )
    }

    // BUDGET OPERATIONS

    suspend fun insertBudget(budget: Budget) = withContext(ioDispatcher) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(ExpenseDbHelper.COLUMN_CATEGORY, budget.category.name)
            put(ExpenseDbHelper.COLUMN_AMOUNT, budget.amount)
            put(ExpenseDbHelper.COLUMN_MONTH_YEAR, budget.dateRange)
        }

        // First check if budget already exists
        val selection = "${ExpenseDbHelper.COLUMN_CATEGORY} = ? AND ${ExpenseDbHelper.COLUMN_MONTH_YEAR} = ?"
        val selectionArgs = arrayOf(budget.category.name, budget.dateRange)

        val cursor = db.query(
            ExpenseDbHelper.TABLE_BUDGETS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val exists = cursor.count > 0
        cursor.close()

        if (exists) {
            // Update existing budget
            db.update(
                ExpenseDbHelper.TABLE_BUDGETS,
                values,
                selection,
                selectionArgs
            )
        } else {
            // Insert new budget
            db.insert(ExpenseDbHelper.TABLE_BUDGETS, null, values)
        }

        refreshBudgets()
    }

    private suspend fun getAllBudgetsFromDb(): List<Budget> = withContext(ioDispatcher) {
        val budgets = mutableListOf<Budget>()
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            ExpenseDbHelper.COLUMN_CATEGORY,
            ExpenseDbHelper.COLUMN_AMOUNT,
            ExpenseDbHelper.COLUMN_MONTH_YEAR
        )

        val cursor = db.query(
            ExpenseDbHelper.TABLE_BUDGETS,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        while (cursor.moveToNext()) {
            val categoryIndex = cursor.getColumnIndexOrThrow(ExpenseDbHelper.COLUMN_CATEGORY)
            val amountIndex = cursor.getColumnIndexOrThrow(ExpenseDbHelper.COLUMN_AMOUNT)
            val monthYearIndex = cursor.getColumnIndexOrThrow(ExpenseDbHelper.COLUMN_MONTH_YEAR)

            budgets.add(
                Budget(
                    category = try {
                        ExpenseCategory.valueOf(cursor.getString(categoryIndex))
                    } catch (e: Exception) {
                        ExpenseCategory.OTHERS
                    },
                    amount = cursor.getDouble(amountIndex),
                    dateRange = cursor.getString(monthYearIndex)
                )
            )
        }

        cursor.close()

        budgets
    }

    // This method will get budgets for a specific month
    fun getBudgetsForMonth(dateRange: DateRange): List<Budget> {
        return budgets.value.filter { it.dateRange == dateRange.toFormattedString() }
    }
}