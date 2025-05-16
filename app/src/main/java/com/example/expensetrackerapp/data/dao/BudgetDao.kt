// 3. Create BudgetDao.kt in com.example.expensetrackerapp.data.dao package
package com.example.expensetrackerapp.data.dao

import androidx.room.*
import com.example.expensetrackerapp.data.model.Budget
import com.example.expensetrackerapp.data.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Update
    suspend fun updateBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun getBudgetsByMonth(monthYear: String): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE category = :category AND monthYear = :monthYear")
    fun getBudgetForCategoryAndMonth(category: ExpenseCategory, monthYear: String): Flow<Budget?>
}