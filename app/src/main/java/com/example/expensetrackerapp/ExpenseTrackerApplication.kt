package com.example.expensetrackerapp

import android.app.Application
import com.example.expensetrackerapp.data.repository.InMemoryExpenseRepository
import com.jakewharton.threetenabp.AndroidThreeTen

class ExpenseTrackerApplication : Application() {
    // In-memory repository
    val repository = InMemoryExpenseRepository()

    override fun onCreate() {
        super.onCreate()
        // Initialize ThreeTenABP for date handling
        AndroidThreeTen.init(this)
    }
}