package com.example.expensetrackerapp.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.example.expensetrackerapp.data.model.Budget
import com.example.expensetrackerapp.data.model.DateRange
import com.example.expensetrackerapp.data.model.Expense
import com.example.expensetrackerapp.data.model.ExpenseCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate

/**
 * Repository implementation for budget operations.
 * This is an enhanced version that ensures proper handling of date ranges.
 */
class BudgetRepository(private val dbHelper: ExpenseDbHelper) {
    private val TAG = "BudgetRepository"
    private val ioDispatcher = Dispatchers.IO.limitedParallelism(1) // Single thread for DB operations

    // StateFlow to provide reactive data access
    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets

    init {
        // Load initial data
        MainScope().launch {
            refreshBudgets()
        }
    }

    // Refreshes the budgets StateFlow with current database data
    suspend fun refreshBudgets() = withContext(ioDispatcher) {
        try {
            val budgets = getAllBudgetsFromDb()
            _budgets.update { budgets }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing budgets: ${e.message}", e)
        }
    }

    /**
     * Insert or update a budget for a specific category and date range
     */
    suspend fun saveBudget(budget: Budget) = withContext(ioDispatcher) {
        try {
            Log.d(TAG, "Saving budget: $budget")
            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {
                put(ExpenseDbHelper.COLUMN_CATEGORY, budget.category.name)
                put(ExpenseDbHelper.COLUMN_AMOUNT, budget.amount)
                // Always use DATE_RANGE column
                put(ExpenseDbHelper.COLUMN_DATE_RANGE, budget.dateRange)
                // Also set MONTH_YEAR for backward compatibility
                put(ExpenseDbHelper.COLUMN_MONTH_YEAR, budget.dateRange)
            }

            // First check if this budget already exists
            val selection = "${ExpenseDbHelper.COLUMN_CATEGORY} = ? AND ${ExpenseDbHelper.COLUMN_DATE_RANGE} = ?"
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
                Log.d(TAG, "Updating existing budget")
                val updated = db.update(
                    ExpenseDbHelper.TABLE_BUDGETS,
                    values,
                    selection,
                    selectionArgs
                )
                Log.d(TAG, "Updated $updated rows")
            } else {
                // Insert new budget
                Log.d(TAG, "Inserting new budget")
                val newId = db.insert(ExpenseDbHelper.TABLE_BUDGETS, null, values)
                Log.d(TAG, "Inserted with ID: $newId")
            }

            // Refresh the budgets after changes
            refreshBudgets()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving budget: ${e.message}", e)
        }
    }

    /**
     * Get budgets for a specific date range
     */
    suspend fun getBudgetsForRange(dateRange: DateRange): List<Budget> = withContext(ioDispatcher) {
        val rangeString = dateRange.toFormattedString()
        Log.d(TAG, "Getting budgets for range: $rangeString")

        try {
            val db = dbHelper.readableDatabase

            val selection = "${ExpenseDbHelper.COLUMN_DATE_RANGE} = ?"
            val selectionArgs = arrayOf(rangeString)

            val cursor = db.query(
                ExpenseDbHelper.TABLE_BUDGETS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
            )

            val budgets = mutableListOf<Budget>()
            while (cursor.moveToNext()) {
                try {
                    budgets.add(cursorToBudget(cursor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing budget: ${e.message}", e)
                }
            }
            cursor.close()

            // If no budgets found for this range, create default ones
            if (budgets.isEmpty()) {
                Log.d(TAG, "No budgets found for range, creating defaults")
                ExpenseCategory.values().forEach { category ->
                    budgets.add(Budget(category, 0.0, rangeString))
                }
            }

            budgets
        } catch (e: Exception) {
            Log.e(TAG, "Error getting budgets for range: ${e.message}", e)
            emptyList()
        }
    }

    private suspend fun getAllBudgetsFromDb(): List<Budget> = withContext(ioDispatcher) {
        val budgets = mutableListOf<Budget>()
        var cursor: Cursor? = null

        try {
            val db = dbHelper.readableDatabase

            cursor = db.query(
                ExpenseDbHelper.TABLE_BUDGETS,
                null,
                null,
                null,
                null,
                null,
                null
            )

            while (cursor.moveToNext()) {
                try {
                    budgets.add(cursorToBudget(cursor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing budget from cursor: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all budgets: ${e.message}", e)
        } finally {
            cursor?.close()
        }

        budgets
    }

    private fun cursorToBudget(cursor: Cursor): Budget {
        val categoryIndex = cursor.getColumnIndexOrThrow(ExpenseDbHelper.COLUMN_CATEGORY)
        val amountIndex = cursor.getColumnIndexOrThrow(ExpenseDbHelper.COLUMN_AMOUNT)

        // Try to get DATE_RANGE first, fall back to MONTH_YEAR if needed
        val dateRangeIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_DATE_RANGE)
        val monthYearIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_MONTH_YEAR)

        val dateRangeValue = if (dateRangeIndex != -1 && !cursor.isNull(dateRangeIndex)) {
            cursor.getString(dateRangeIndex)
        } else if (monthYearIndex != -1 && !cursor.isNull(monthYearIndex)) {
            cursor.getString(monthYearIndex)
        } else {
            // Fallback to current period if neither is available
            DateRange.customDefault().toFormattedString()
        }

        return Budget(
            category = try {
                ExpenseCategory.valueOf(cursor.getString(categoryIndex))
            } catch (e: Exception) {
                Log.w(TAG, "Unknown budget category, using OTHERS: ${e.message}")
                ExpenseCategory.OTHERS
            },
            amount = cursor.getDouble(amountIndex),
            dateRange = dateRangeValue
        )
    }
}