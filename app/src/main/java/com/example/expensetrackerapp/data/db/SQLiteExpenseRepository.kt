// FILE: app/src/main/java/com/example/expensetrackerapp/data/db/SQLiteExpenseRepository.kt
package com.example.expensetrackerapp.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.expensetrackerapp.data.model.Budget
import com.example.expensetrackerapp.data.model.Expense
import com.example.expensetrackerapp.data.model.ExpenseCategory
import com.example.expensetrackerapp.data.model.MonthYear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.threeten.bp.LocalDate
import java.util.concurrent.Executors

/**
 * Repository implementation using SQLite directly.
 * Handles data access operations for expenses and budgets.
 */
class SQLiteExpenseRepository(context: Context) {
    private val dbHelper = ExpenseDbHelper(context)
    private val executor = Executors.newSingleThreadExecutor()

    // StateFlows to provide reactive data access
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets

    init {
        // Load initial data
        refreshExpenses()
        refreshBudgets()
    }

    // Refreshes the expenses StateFlow with current database data
    private fun refreshExpenses() {
        executor.execute {
            val expenses = getAllExpensesFromDb()
            _expenses.update { expenses }
        }
    }

    // Refreshes the budgets StateFlow with current database data
    private fun refreshBudgets() {
        executor.execute {
            val budgets = getAllBudgetsFromDb()
            _budgets.update { budgets }
        }
    }

    // EXPENSE OPERATIONS

    suspend fun insertExpense(expense: Expense): Long {
        var newId: Long = 0
        executor.execute {
            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {
                put(ExpenseDbHelper.COLUMN_CATEGORY, expense.category.name)
                put(ExpenseDbHelper.COLUMN_AMOUNT, expense.amount)
                put(ExpenseDbHelper.COLUMN_DESCRIPTION, expense.description)
                put(ExpenseDbHelper.COLUMN_DATE, expense.date.toString())
                expense.receiptUri?.let { put(ExpenseDbHelper.COLUMN_RECEIPT_URI, it) }
            }

            newId = db.insert(ExpenseDbHelper.TABLE_EXPENSES, null, values)
            refreshExpenses()
        }

        // Give the executor a moment to complete the operation
        Thread.sleep(100)
        return newId
    }

    suspend fun updateExpense(expense: Expense) {
        executor.execute {
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

        // Give the executor a moment to complete the operation
        Thread.sleep(100)
    }

    suspend fun deleteExpense(expense: Expense) {
        executor.execute {
            val db = dbHelper.writableDatabase

            val selection = "${ExpenseDbHelper.COLUMN_ID} = ?"
            val selectionArgs = arrayOf(expense.id.toString())

            db.delete(ExpenseDbHelper.TABLE_EXPENSES, selection, selectionArgs)

            refreshExpenses()
        }

        // Give the executor a moment to complete the operation
        Thread.sleep(100)
    }

    suspend fun getExpenseById(id: Long): Expense? {
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

        return expense
    }

    private fun getAllExpensesFromDb(): List<Expense> {
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

        return expenses
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
            category = ExpenseCategory.valueOf(cursor.getString(categoryIndex)),
            amount = cursor.getDouble(amountIndex),
            description = cursor.getString(descriptionIndex),
            date = LocalDate.parse(cursor.getString(dateIndex)),
            receiptUri = if (cursor.isNull(receiptUriIndex)) null else cursor.getString(receiptUriIndex)
        )
    }

    // BUDGET OPERATIONS

    suspend fun insertBudget(budget: Budget) {
        executor.execute {
            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {
                put(ExpenseDbHelper.COLUMN_CATEGORY, budget.category.name)
                put(ExpenseDbHelper.COLUMN_AMOUNT, budget.amount)
                put(ExpenseDbHelper.COLUMN_MONTH_YEAR, budget.monthYear)
            }

            // First check if budget already exists
            val selection = "${ExpenseDbHelper.COLUMN_CATEGORY} = ? AND ${ExpenseDbHelper.COLUMN_MONTH_YEAR} = ?"
            val selectionArgs = arrayOf(budget.category.name, budget.monthYear)

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

        // Give the executor a moment to complete the operation
        Thread.sleep(100)
    }

    private fun getAllBudgetsFromDb(): List<Budget> {
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
                    category = ExpenseCategory.valueOf(cursor.getString(categoryIndex)),
                    amount = cursor.getDouble(amountIndex),
                    monthYear = cursor.getString(monthYearIndex)
                )
            )
        }

        cursor.close()

        return budgets
    }

    // This method will get budgets for a specific month
    fun getBudgetsForMonth(monthYear: MonthYear): List<Budget> {
        return budgets.value.filter { it.monthYear == monthYear.toFormattedString() }
    }
}