package com.example.expensetrackerapp

import android.app.Application
import com.example.expensetrackerapp.data.db.SQLiteExpenseRepository
import com.jakewharton.threetenabp.AndroidThreeTen

class ExpenseTrackerApplication : Application() {
    // SQLite repository
    val repository by lazy { SQLiteExpenseRepository(this) }

    override fun onCreate() {
        super.onCreate()
        // Initialize ThreeTenABP for date handling
        AndroidThreeTen.init(this)
    }
}