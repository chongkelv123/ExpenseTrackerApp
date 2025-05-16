// 2. Create Expense.kt in com.example.expensetrackerapp.data.model package
package com.example.expensetrackerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.expensetrackerapp.util.DateConverter
import java.time.LocalDate

@Entity(tableName = "expenses")
@TypeConverters(DateConverter::class)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: ExpenseCategory,
    val amount: Double,
    val description: String,
    val date: LocalDate,
    val receiptUri: String? = null
)