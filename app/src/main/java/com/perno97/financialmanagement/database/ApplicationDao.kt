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

    @Query("SELECT assets AS value FROM profile WHERE profileId = :defaultProfileId")
    suspend fun getCurrentAsset(defaultProfileId: Int): Float

    @Query("SELECT * FROM category WHERE name = :categoryName")
    fun getCategory(categoryName: String): Flow<Category>

    @Transaction
    @Query("SELECT * FROM movement")
    fun getMovementAndCategory(): Flow<List<MovementAndCategory>>

    @Query("SELECT * FROM category")
    suspend fun getCategoryWithMovements(): List<CategoryWithMovements>


    /*
    Advanced getters
     */
    @Query(
        "SELECT name, color, daily_budget, current AS expense FROM category" +
                " INNER JOIN (SELECT movement.category AS catName, SUM(CASE WHEN movement.amount < 0 THEN movement.amount else 0 END) AS current FROM" +
                " movement WHERE date >= :dateFrom AND date <= :dateTo GROUP BY catName)" +
                " ON category.name = catName WHERE daily_budget > 0"
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
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'start of month') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.*, category.* FROM movement a" +
                " JOIN movement b ON STRFTIME('%Y-%m', a.date,'unixepoch') = STRFTIME('%Y-%m', b.date,'unixepoch')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date AND :beforeDateInclusive >= a.date GROUP BY b.movementId ORDER BY groupDate DESC, b.date DESC"
    )
    fun getMovementsGroupByMonth(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<MovementAndCategory>>>

    @Transaction
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.*, category.* FROM movement a" +
                " JOIN movement b ON STRFTIME('%Y-%m-%d', a.date,'unixepoch') = STRFTIME('%Y-%m-%d', b.date,'unixepoch')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date AND :beforeDateInclusive >= a.date GROUP BY b.movementId ORDER BY groupDate DESC, b.date DESC"
    )
    fun getMovementsGroupByDay(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<MovementAndCategory>>>

    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.*, category.* FROM movement a" +
                " JOIN movement b ON STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days')" +
                " = STRFTIME('%Y-%m-%d', b.date,'unixepoch',  'weekday 0', '-' || :weekStartOffset ||' days')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusiveInclusive >= b.date AND :beforeDateInclusiveInclusive >= a.date GROUP BY b.movementId ORDER BY groupDate DESC, b.date DESC"
    )
    fun getMovementsGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusiveInclusive: LocalDate
    ): Flow<Map<GroupInfo, List<MovementAndCategory>>>
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
                " JOIN movement ON name = movement.category WHERE name IN (:categories) AND :beforeDateInclusive >= movement.date" +
                " GROUP BY STRFTIME('%Y-%m', movement.date, 'unixepoch') LIMIT 12"
    )
    fun getCategoriesExpensesMonth(categories: List<String>, beforeDateInclusive: LocalDate): Flow<Map<Category, List<AmountWithDate>>>

    @Query(
        "SELECT category.*, SUM(CASE WHEN amount < 0 THEN amount else 0 END) AS expense, " +
                "SUM(CASE WHEN amount > 0 THEN amount else 0 END) AS gain, movement.date AS amountDate " +
                "FROM category JOIN movement ON name = movement.category WHERE name IN (:categories)" +
                " GROUP BY STRFTIME('%Y-%m-%d', movement.date,'unixepoch',  'weekday 0', '-' || :weekStartOffset ||' days') LIMIT 12"
    )
    fun getCategoriesExpensesWeek(
        categories: List<String>,
        weekStartOffset: Int
    ): Flow<Map<Category, List<AmountWithDate>>>

    @Query(
        "SELECT category.*, SUM(CASE WHEN amount < 0 THEN amount else 0 END) AS expense, " +
                "SUM(CASE WHEN amount > 0 THEN amount else 0 END) AS gain, movement.date AS amountDate FROM category" +
                " JOIN movement ON name = movement.category WHERE name IN (:categories)" +
                " AND movement.date >= :dateFrom AND movement.date <= :dateTo" +
                " GROUP BY movement.date LIMIT 12"
    )
    fun getCategoriesExpensesPeriod(
        categories: List<String>,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<Category, List<AmountWithDate>>>

    /*
    LEFT JOIN in order to return category data with some null columns if there's no movement
    in the selected period
     */
    @Query(
        "SELECT name, color, daily_budget, positive, negative FROM category LEFT JOIN " +
                "(SELECT movement.category AS catName, SUM(CASE WHEN amount > 0 THEN amount else 0 END) AS positive," +
                " SUM(CASE WHEN amount < 0 THEN amount else 0 END) AS negative FROM" +
                " movement WHERE date >= :dateFrom AND date <= :dateTo GROUP BY catName)" +
                " ON category.name = catName"
    )
    fun getCategoryProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<Category, PositiveNegativeSums>>

    @Query(
        "SELECT SUM(movement.amount) AS value FROM movement WHERE movement.date >= :dateFrom AND movement.date <= :dateTo"
    )
    fun getExpectedSum(dateFrom: LocalDate, dateTo: LocalDate): Flow<Float>
}