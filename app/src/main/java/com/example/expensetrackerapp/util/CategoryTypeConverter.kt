package com.example.expensetrackerapp.util

import androidx.room.TypeConverter
import com.example.expensetrackerapp.data.model.ExpenseCategory

class CategoryTypeConverter {
    @TypeConverter
    fun fromCategory(category: ExpenseCategory): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(value: String): ExpenseCategory {
        return ExpenseCategory.valueOf(value)
    }
}