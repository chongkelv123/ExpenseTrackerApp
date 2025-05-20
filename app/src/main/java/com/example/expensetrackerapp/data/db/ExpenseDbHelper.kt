package com.example.expensetrackerapp.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLite database helper for the ExpenseTracker app.
 * Handles database creation and schema updates.
 */
class ExpenseDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 2  // Incremented version number
        const val DATABASE_NAME = "ExpenseTracker.db"

        // Table names
        const val TABLE_EXPENSES = "expenses"
        const val TABLE_BUDGETS = "budgets"

        // Column names
        const val COLUMN_ID = "id"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_DATE = "date"
        const val COLUMN_RECEIPT_URI = "receipt_uri"
        const val COLUMN_MONTH_YEAR = "month_year"  // For backward compatibility
        const val COLUMN_DATE_RANGE = "date_range"  // New column for date ranges

        // SQL statements
        private const val SQL_CREATE_EXPENSES_TABLE = """
            CREATE TABLE $TABLE_EXPENSES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_RECEIPT_URI TEXT
            )
        """

        private const val SQL_CREATE_BUDGETS_TABLE = """
            CREATE TABLE $TABLE_BUDGETS (
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_MONTH_YEAR TEXT,
                $COLUMN_DATE_RANGE TEXT,
                PRIMARY KEY ($COLUMN_CATEGORY, COALESCE($COLUMN_DATE_RANGE, $COLUMN_MONTH_YEAR))
            )
        """

        private const val SQL_ALTER_BUDGETS_TABLE = """
            ALTER TABLE $TABLE_BUDGETS ADD COLUMN $COLUMN_DATE_RANGE TEXT
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_EXPENSES_TABLE)
        db.execSQL(SQL_CREATE_BUDGETS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Migration to add date_range column
            db.execSQL(SQL_ALTER_BUDGETS_TABLE)
        }
    }
}