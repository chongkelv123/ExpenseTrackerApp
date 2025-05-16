// 8. Create ExpenseTrackerApplication.kt in com.example.expensetrackerapp package
package com.example.expensetrackerapp

import android.app.Application
import com.example.expensetrackerapp.data.database.AppDatabase
import com.example.expensetrackerapp.data.repository.BudgetRepository
import com.example.expensetrackerapp.data.repository.ExpenseRepository
import com.jakewharton.threetenabp.AndroidThreeTen

class ExpenseTrackerApplication : Application() {
    // Database and repository instances
    val database by lazy { AppDatabase.getDatabase(this) }
    val expenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
    val budgetRepository by lazy { BudgetRepository(database.budgetDao()) }

    override fun onCreate() {
        super.onCreate()
        // Initialize ThreeTenABP for date handling
        AndroidThreeTen.init(this)
    }
}