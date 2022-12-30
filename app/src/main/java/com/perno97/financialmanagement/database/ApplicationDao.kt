package com.perno97.financialmanagement.database

import androidx.room.*
import com.perno97.financialmanagement.viewmodels.PositiveNegativeSums
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.*

@Dao
interface ApplicationDao {

    /*
    Insert
     */
    @Insert
    suspend fun insertCategories(vararg categories: Category)

    @Insert
    suspend fun insertMovements(vararg movements: Movement)

    @Insert
    suspend fun insertPeriodicMovements(vararg periodicMovements: PeriodicMovement)

    @Insert
    suspend fun insertIncumbentMovements(vararg incomingMovements: IncomingMovement)

    @Insert
    suspend fun insertProfiles(vararg profiles: Profile)


    /*
    Update
     */
    @Update
    suspend fun updateProfiles(vararg profiles: Profile)

    @Update
    suspend fun updateCategories(vararg categories: Category)

    @Update
    suspend fun updateMovements(vararg movements: Movement)

    @Update
    suspend fun updatePeriodicMovements(vararg periodicMovements: PeriodicMovement)

    @Update
    suspend fun updateIncumbentMovements(vararg incomingMovements: IncomingMovement)


    /*
    Delete
     */
    @Delete
    suspend fun deleteCategory(category: Category)

    @Query(
        "DELETE FROM movement WHERE movement_id = :movementId"
    )
    suspend fun deleteMovementFromId(movementId: Int)

    @Query(
        "DELETE FROM incoming_movement WHERE incoming_movement_id = :incomingMovementId"
    )
    suspend fun deleteIncumbentMovementFromId(incomingMovementId: Int)


    /*
    Basic getters
     */
    @Query("SELECT * FROM category")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM movement")
    fun getAllMovements(): Flow<List<Movement>>

    @Query("SELECT * FROM periodic_movement")
    suspend fun getAllPeriodicMovements(): List<PeriodicMovement>

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
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date AND :beforeDateInclusive >= a.date GROUP BY b.movement_id ORDER BY groupDate DESC, b.date DESC"
    )
    fun getMovementsGroupByMonth(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<MovementAndCategory>>>

    @Transaction
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'start of month') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.*, category.* FROM incoming_movement a" +
                " JOIN incoming_movement b ON STRFTIME('%Y-%m', a.date,'unixepoch') = STRFTIME('%Y-%m', b.date,'unixepoch')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date AND :beforeDateInclusive >= a.date GROUP BY b.incoming_movement_id ORDER BY groupDate DESC, b.date DESC"
    )
    fun getIncomingMovementsGroupByMonth(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>>

    @Transaction
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.*, category.* FROM movement a" +
                " JOIN movement b ON STRFTIME('%Y-%m-%d', a.date,'unixepoch') = STRFTIME('%Y-%m-%d', b.date,'unixepoch')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date AND :beforeDateInclusive >= a.date GROUP BY b.movement_id ORDER BY groupDate DESC, b.date DESC"
    )
    fun getMovementsGroupByDay(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<MovementAndCategory>>>


    @Transaction
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.*, category.* FROM incoming_movement a" +
                " JOIN incoming_movement b ON STRFTIME('%Y-%m-%d', a.date,'unixepoch') = STRFTIME('%Y-%m-%d', b.date,'unixepoch')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date AND :beforeDateInclusive >= a.date GROUP BY b.incoming_movement_id ORDER BY groupDate DESC, b.date DESC"
    )
    fun getIncomingMovementsGroupByDay(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>>


    // move to sunday then move to the first day of the week, if they're equal the events are in the same week
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.*, category.* FROM movement a" +
                " JOIN movement b ON STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days')" +
                " = STRFTIME('%Y-%m-%d', b.date,'unixepoch',  'weekday 0', '-' || :weekStartOffset ||' days')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date AND :beforeDateInclusive >= a.date GROUP BY b.movement_id ORDER BY groupDate DESC, b.date DESC"
    )
    fun getMovementsGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate
    ): Flow<Map<GroupInfo, List<MovementAndCategory>>>


    // move to sunday then move to the first day of the week, if they're equal the events are in the same week
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.*, category.* FROM incoming_movement a" +
                " JOIN incoming_movement b ON STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days')" +
                " = STRFTIME('%Y-%m-%d', b.date,'unixepoch',  'weekday 0', '-' || :weekStartOffset ||' days')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date AND :beforeDateInclusive >= a.date GROUP BY b.incoming_movement_id ORDER BY groupDate DESC, b.date DESC"
    )
    fun getIncomingMovementsGroupByWeek(weekStartOffset: Int, beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>>


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
        "SELECT '' AS groupDate," +
                " (SELECT SUM(CASE WHEN amount > 0 THEN amount else 0 END) FROM  movement WHERE date >= :dateFrom AND date <= :dateTo) AS positive," +
                " (SELECT SUM(CASE WHEN amount < 0 THEN amount else 0 END) FROM movement WHERE date >= :dateFrom AND date <= :dateTo) AS negative," +
                " incoming_movement.*, category.* FROM incoming_movement " +
                " JOIN category ON category = name" +
                " WHERE date >= :dateFrom AND date <= :dateTo ORDER BY date DESC"
    )
    fun getIncomingMovementsInPeriod(dateFrom: LocalDate, dateTo: LocalDate): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>>

    @Query(
        "SELECT STRFTIME('%Y-%m-%d', movement.date,'unixepoch', 'start of month') AS groupDate," +
                " SUM(CASE WHEN movement.amount > 0 THEN movement.amount else 0 END) AS positive," +
                " SUM(CASE WHEN movement.amount < 0 THEN movement.amount else 0 END) AS negative FROM movement" +
                " WHERE :beforeDateInclusive >= movement.date GROUP BY groupDate ORDER BY groupDate DESC"
    )
    fun getMovementsSumGroupByMonth(beforeDateInclusive: LocalDate): Flow<List<GroupInfo>>


    // move to sunday then move to the first day of the week, if they're equal the events are in the same week
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', movement.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days') AS groupDate," +
                " SUM(CASE WHEN movement.amount > 0 THEN movement.amount else 0 END) AS positive," +
                " SUM(CASE WHEN movement.amount < 0 THEN movement.amount else 0 END) AS negative FROM movement" +
                " WHERE :beforeDateInclusive >= movement.date GROUP BY groupDate ORDER BY groupDate DESC"
    )
    fun getMovementsSumGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate
    ): Flow<List<GroupInfo>>


    @Query(
        "SELECT STRFTIME('%Y-%m-%d', movement.date,'unixepoch') AS groupDate, " +
                "SUM(CASE WHEN amount > 0 THEN amount else 0 END) AS positive, " +
                "SUM(CASE WHEN amount < 0 THEN amount else 0 END) AS negative" +
                " FROM movement WHERE date >= :dateFrom AND date <= :dateTo GROUP BY groupDate ORDER BY date DESC"
    )
    fun getMovementsSumInPeriod(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<List<GroupInfo>>


    @Query(
        "SELECT category.*, SUM(CASE WHEN amount < 0 THEN amount else 0 END) AS expense, " +
                "SUM(CASE WHEN amount > 0 THEN amount else 0 END) AS gain, movement.date AS amountDate FROM category" +
                " JOIN movement ON name = movement.category WHERE name IN (:categories) AND :beforeDateInclusive >= movement.date" +
                " GROUP BY STRFTIME('%Y-%m', movement.date, 'unixepoch') LIMIT 12"
    )
    fun getCategoriesExpensesMonth(
        categories: List<String>,
        beforeDateInclusive: LocalDate
    ): Flow<Map<Category, List<AmountWithDate>>>

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
                " LEFT JOIN movement ON name = movement.category WHERE name IN (:categories)" +
                " AND movement.date >= :dateFrom AND movement.date <= :dateTo" +
                " GROUP BY movement.date"
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

    @Query(
        "SELECT '' AS groupDate," +
                " SUM(CASE WHEN amount > 0 THEN amount else 0 END) AS positive," +
                " SUM(CASE WHEN amount < 0 THEN amount else 0 END) AS negative" +
                " FROM movement WHERE :dateTo >= movement.date AND movement.date >= :dateFrom"
    )
    fun getGainsAndExpensesInPeriod(dateFrom: LocalDate, dateTo: LocalDate): Flow<GroupInfo>

    @Query(
        "SELECT * FROM movement WHERE periodic_movement_id = :periodicMovementId AND :dateTo >= date AND date >= :dateFrom ORDER BY date DESC LIMIT 1"
    )
    suspend fun getLatestPeriodicMovement(
        periodicMovementId: Int,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Movement?

    @Query(
        "SELECT * FROM periodic_movement WHERE category = :categoryName"
    )
    suspend fun getPeriodicMovements(categoryName: String): List<PeriodicMovement>
}