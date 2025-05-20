package com.example.expensetrackerapp.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * SQLite database helper for the ExpenseTracker app.
 * Handles database creation and schema updates.
 */
class ExpenseDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "ExpenseDbHelper"
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
        try {
            db.execSQL(SQL_CREATE_EXPENSES_TABLE)
            db.execSQL(SQL_CREATE_BUDGETS_TABLE)
            Log.i(TAG, "Database tables created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating database tables: ${e.message}", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            Log.i(TAG, "Upgrading database from version $oldVersion to $newVersion")

            if (oldVersion < 2) {
                // Check if COLUMN_DATE_RANGE already exists to avoid errors
                val cursor = db.rawQuery(
                    "PRAGMA table_info($TABLE_BUDGETS)",
                    null
                )

                val columnNames = mutableListOf<String>()
                if (cursor.moveToFirst()) {
                    do {
                        val columnNameIndex = cursor.getColumnIndex("name")
                        if (columnNameIndex != -1) {
                            columnNames.add(cursor.getString(columnNameIndex))
                        }
                    } while (cursor.moveToNext())
                }
                cursor.close()

                // Only add the column if it doesn't exist
                if (!columnNames.contains(COLUMN_DATE_RANGE)) {
                    // Migration to add date_range column
                    db.execSQL(SQL_ALTER_BUDGETS_TABLE)
                    Log.i(TAG, "Added $COLUMN_DATE_RANGE column to $TABLE_BUDGETS table")

                    // Update existing records to have date_range = month_year
                    db.execSQL(
                        "UPDATE $TABLE_BUDGETS SET $COLUMN_DATE_RANGE = $COLUMN_MONTH_YEAR " +
                                "WHERE $COLUMN_DATE_RANGE IS NULL"
                    )
                    Log.i(TAG, "Updated existing budget records with date range values")
                }
            }
        } catch (e: Exception) {
            // Log error but don't crash - the app might still work with the old schema
            Log.e(TAG, "Error during database upgrade: ${e.message}", e)
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // Enable foreign key constraints
        if (!db.isReadOnly) {
            db.execSQL("PRAGMA foreign_keys = ON")
        }
    }
}