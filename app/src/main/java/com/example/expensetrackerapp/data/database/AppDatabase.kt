// 1. Create AppDatabase.kt file in com.example.expensetrackerapp.data.database package
package com.example.expensetrackerapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.expensetrackerapp.data.dao.ExpenseDao
import com.example.expensetrackerapp.data.dao.BudgetDao
import com.example.expensetrackerapp.data.model.Expense
import com.example.expensetrackerapp.data.model.Budget
import com.example.expensetrackerapp.util.DateConverter

@Database(entities = [Expense::class, Budget::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}