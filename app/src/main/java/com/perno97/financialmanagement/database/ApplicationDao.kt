package com.perno97.financialmanagement.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.*

@Dao
interface ApplicationDao {

    @Query("SELECT * FROM category")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM movement")
    fun getAllMovements(): Flow<List<Movement>>

    @Insert
    suspend fun insertAllCategories(vararg categories: Category)

    @Insert
    suspend fun insertAllMovements(vararg movements: Movement)

    @Query("DELETE FROM category")
    suspend fun deleteAllCategories()

    @Query("SELECT name, color, daily_budget AS budget, current FROM category" +
            " INNER JOIN (SELECT movement.category AS catName, SUM(amount) AS current FROM" +
            " movement WHERE date >= :dateFrom AND date <= :dateTo AND amount > 0 GROUP BY catName)" +
            " ON category.name = catName")
    fun getCategoryBudgetsList(dateFrom: LocalDate, dateTo: LocalDate): Flow<List<CategoryWithExpensesSum>>
}