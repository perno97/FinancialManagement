package com.perno97.financialmanagement.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.perno97.financialmanagement.viewmodels.PositiveNegativeSums
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.*

@Dao
interface ApplicationDao {

    /*
    Insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(vararg categories: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(vararg movements: Movement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(vararg profiles: Profile)


    /*
    Update
     */
    @Update
    suspend fun updateProfiles(vararg profiles: Profile)


    /*
    Delete
     */
    @Delete
    suspend fun deleteCategory(category: Category)


    /*
    Basic getters
     */
    @Query("SELECT * FROM category")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM movement")
    fun getAllMovements(): Flow<List<Movement>>

    @Query("SELECT SUM(daily_budget) as budget FROM category")
    fun getAvailableDailyBudget(): Flow<Float>

    @Query("SELECT * FROM profile WHERE profileId = :profileId")
    fun getProfile(profileId: Int): Flow<Profile>

    @Query("SELECT * FROM category WHERE name = :categoryName")
    fun getCategory(categoryName: String): Flow<Category>

    @Transaction
    @Query("SELECT * FROM movement")
    fun getMovementAndCategory(): Flow<List<MovementAndCategory>>

    @Transaction
    @Query("SELECT * FROM category")
    fun getCategoryWithMovements(): Flow<List<CategoryWithMovements>>


    /*
    Advanced getters
     */
    @Query(
        "SELECT name, color, daily_budget, current AS expense FROM category" +
                " INNER JOIN (SELECT movement.category AS catName, SUM(amount) AS current FROM" +
                " movement WHERE date >= :dateFrom AND date <= :dateTo AND amount < 0 GROUP BY catName)" +
                " ON category.name = catName"
    )
    fun getCategoryExpensesProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<Category, Expense>>

    @Query(
        "SELECT SUM(amount) AS expense FROM movement WHERE date >= :dateFrom AND date <= :dateTo AND amount < 0 AND movement.category LIKE :categoryName"
    )
    fun getCategoryExpensesProgress(
        dateFrom: LocalDate,
        dateTo: LocalDate,
        categoryName: String
    ): Flow<Expense>

    @Transaction
    @Query(
        "SELECT a.date AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.*, category.* FROM movement a" +
                " JOIN movement b ON STRFTIME('%Y-%m', a.date,'unixepoch') = STRFTIME('%Y-%m', b.date,'unixepoch')" +
                " JOIN category ON b.category = category.name GROUP BY b.movementId ORDER BY groupDate DESC"
    )
    fun getMovementsGroupByMonth(): Flow<Map<GroupInfo, List<MovementAndCategory>>>

    @Transaction
    @Query(
        "SELECT a.date AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.*, category.* FROM movement a" +
                " JOIN movement b ON STRFTIME('%Y-%m-%d', a.date,'unixepoch') = STRFTIME('%Y-%m-%d', b.date,'unixepoch')" +
                " JOIN category ON b.category = category.name GROUP BY b.movementId ORDER BY groupDate DESC"
    )
    fun getMovementsGroupByDay(): Flow<Map<GroupInfo, List<MovementAndCategory>>>

    @Query(
        "SELECT a.date AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.*, category.* FROM movement a" +
                " JOIN movement b ON STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days')" +
                " = STRFTIME('%Y-%m-%d', b.date,'unixepoch',  'weekday 0', '-' || :weekStartOffset ||' days')" +
                " JOIN category ON b.category = category.name GROUP BY b.movementId ORDER BY groupDate DESC"
    )
    fun getMovementsGroupByWeek(weekStartOffset: Int): Flow<Map<GroupInfo, List<MovementAndCategory>>>
    // move to sunday then move to the first day of the week, if they're equal the events are in the same week

    @Query(
        "SELECT '' AS groupDate," +
                " (SELECT SUM(CASE WHEN amount > 0 THEN amount else 0 END) FROM  movement WHERE date >= :dateFrom AND date <= :dateTo) AS positive," +
                " (SELECT SUM(CASE WHEN amount < 0 THEN amount else 0 END) FROM movement WHERE date >= :dateFrom AND date <= :dateTo) AS negative," +
                " movement.*, category.* FROM movement " +
                " JOIN category ON category = name" +
                " WHERE date >= :dateFrom AND date <= :dateTo ORDER BY date DESC"
    )
    fun getMovementsInPeriod(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<GroupInfo, List<MovementAndCategory>>>

    @Query(
        "SELECT category.*, SUM(CASE WHEN amount < 0 THEN amount else 0 END) AS expense, " +
                "SUM(CASE WHEN amount > 0 THEN amount else 0 END) AS gain, movement.date AS amountDate FROM category" +
                " JOIN movement ON name = movement.category WHERE name IN (:categories)" +
                " GROUP BY STRFTIME('%Y-%m', movement.date, 'unixepoch') LIMIT 12"
    )
    fun getCategoriesExpensesMonth(categories: List<String>): Flow<Map<Category, List<AmountWithDate>>>

    @Query(
        "SELECT name, color, daily_budget, positive, negative FROM category INNER JOIN " +
                "(SELECT movement.category AS catName, SUM(CASE WHEN amount > 0 THEN amount else 0 END) AS positive," +
                " SUM(CASE WHEN amount < 0 THEN amount else 0 END) AS negative FROM" +
                " movement WHERE date >= :dateFrom AND date <= :dateTo GROUP BY catName)" +
                " ON category.name = catName"
    )
    fun getCategoryProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<Category, PositiveNegativeSums>>
}