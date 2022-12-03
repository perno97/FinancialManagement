package com.perno97.financialmanagement.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
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

    @Query(
        "SELECT name, color, daily_budget AS budget, current FROM category" +
                " INNER JOIN (SELECT movement.category AS catName, SUM(amount) AS current FROM" +
                " movement WHERE date >= :dateFrom AND date <= :dateTo AND amount < 0 GROUP BY catName)" +
                " ON category.name = catName"
    )
    fun getCategoryExpensesProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<List<CategoryWithExpensesSum>>

    @Transaction
    @Query("SELECT * FROM movement")
    fun getMovementAndCategory(): Flow<List<MovementAndCategory>>

    @Transaction
    @Query(
        "SELECT STRFTIME('%Y-%m', a.date,'unixepoch') AS newDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.*, category.* FROM movement a" +
                " JOIN movement b ON STRFTIME('%Y-%m', a.date,'unixepoch') = STRFTIME('%Y-%m', b.date,'unixepoch')" +
                " JOIN category ON b.category = category.name GROUP BY b.movementId"
    )
    fun getMovementsGroupByMonth(): Flow<Map<GroupInfo, List<MovementAndCategory>>>
}