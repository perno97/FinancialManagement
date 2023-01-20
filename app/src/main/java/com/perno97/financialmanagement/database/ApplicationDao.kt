@file:Suppress("SpellCheckingInspection")

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
    suspend fun insertPeriodicMovement(periodicMovements: PeriodicMovement): Long

    @Insert
    suspend fun insertIncumbentMovement(incomingMovement: IncomingMovement): Long

    @Insert
    suspend fun insertProfiles(vararg profiles: Profile)


    /*
    Update
     */
    @Update
    suspend fun updateCategories(vararg categories: Category)

    @Update
    suspend fun updateMovements(vararg movements: Movement)

    @Update
    suspend fun updatePeriodicMovements(vararg periodicMovements: PeriodicMovement)

    @Update
    suspend fun updateIncomingMovements(vararg incomingMovements: IncomingMovement)

    @Query(
        "UPDATE profile SET assets = :assetsValue WHERE profileId = :profileId"
    )
    suspend fun updateAssets(profileId: Long, assetsValue: Float)

    @Query(
        "UPDATE movement SET category = :newName WHERE category = :oldName"
    )
    suspend fun updateCategoryNameInMovements(oldName: String, newName: String)


    /*
    Delete
     */
    @Delete
    suspend fun deleteCategory(category: Category)

    @Query(
        "DELETE FROM movement WHERE movement_id = :movementId"
    )
    suspend fun deleteMovementFromId(movementId: Long)

    @Query(
        "DELETE FROM incoming_movement WHERE incoming_movement_id = :incomingMovementId"
    )
    suspend fun deleteIncumbentMovementFromId(incomingMovementId: Long)

    @Query(
        "DELETE FROM periodic_movement WHERE periodic_movement_id = :periodicMovementId"
    )
    suspend fun deletePeriodicMovementFromId(periodicMovementId: Long)

    @Query(
        "DELETE FROM incoming_movement WHERE periodic_movement_id = :periodicMovementId"
    )
    suspend fun deleteAllIncomingOfPeriodic(periodicMovementId: Long)


    /*
    Basic getters
     */
    @Query("SELECT * FROM category")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM periodic_movement")
    suspend fun getAllPeriodicMovements(): List<PeriodicMovement>

    @Query("SELECT SUM(daily_budget) as budget FROM category")
    fun getAvailableDailyBudget(): Flow<Float>

    @Query("SELECT * FROM profile WHERE profileId = :profileId")
    fun getProfile(profileId: Long): Flow<Profile>

    @Query("SELECT assets AS value FROM profile WHERE profileId = :profileId")
    suspend fun getCurrentAsset(profileId: Long): Float

    @Query("SELECT last_access FROM profile WHERE profileId = :profileId")
    suspend fun getLastAccess(profileId: Long): LocalDate?

    @Query("SELECT * FROM category WHERE category_id = :categoryId")
    fun getCategory(categoryId: Long): Flow<Category>

    @Transaction
    @Query("SELECT * FROM category")
    suspend fun getCategoryWithMovements(): List<CategoryWithMovements>

    @Query("SELECT COUNT(*) AS value FROM incoming_movement WHERE :beforeDateInclusive >= date")
    fun countIncoming(beforeDateInclusive: LocalDate): Flow<Int>

    @Query("SELECT * FROM periodic_movement WHERE periodic_movement_id = :periodicMovementId")
    suspend fun getPeriodicMovement(periodicMovementId: Long): PeriodicMovement?


    /*
    Advanced getters
     */
    @Query(
        "SELECT category.*, current AS expense FROM category" +
                " LEFT JOIN (SELECT movement.category AS catName, SUM(CASE WHEN movement.amount < 0 THEN movement.amount else 0 END) AS current FROM" +
                " movement WHERE date >= :dateFrom AND date <= :dateTo GROUP BY catName)" +
                " ON category.name = catName WHERE daily_budget > 0"
    )
    fun getCategoryExpensesProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<Category, Expense>>

    @Transaction
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'start of month') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.*FROM movement a" +
                " JOIN movement b ON STRFTIME('%Y-%m', a.date,'unixepoch') = STRFTIME('%Y-%m', b.date,'unixepoch')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date AND :beforeDateInclusive >= a.date GROUP BY b.movement_id ORDER BY groupDate DESC, b.date DESC"
    )
    fun getMovementsGroupByMonth(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<MovementAndCategory>>>

    @Transaction
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'start of month') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.* FROM incoming_movement a" +
                " JOIN incoming_movement b ON STRFTIME('%Y-%m', a.date,'unixepoch') = STRFTIME('%Y-%m', b.date,'unixepoch')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date GROUP BY b.incoming_movement_id ORDER BY groupDate ASC, b.date ASC"
    )
    fun getIncomingMovementsGroupByMonth(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>>

    @Transaction
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.* FROM movement a" +
                " JOIN movement b ON STRFTIME('%Y-%m-%d', a.date,'unixepoch') = STRFTIME('%Y-%m-%d', b.date,'unixepoch')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date AND :beforeDateInclusive >= a.date GROUP BY b.movement_id ORDER BY groupDate DESC, b.date DESC"
    )
    fun getMovementsGroupByDay(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<MovementAndCategory>>>


    @Transaction
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.* FROM incoming_movement a" +
                " JOIN incoming_movement b ON STRFTIME('%Y-%m-%d', a.date,'unixepoch') = STRFTIME('%Y-%m-%d', b.date,'unixepoch')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date GROUP BY b.incoming_movement_id ORDER BY groupDate ASC, b.date ASC"
    )
    fun getIncomingMovementsGroupByDay(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>>


    @Transaction
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.* FROM periodic_movement a" +
                " JOIN periodic_movement b ON STRFTIME('%Y-%m-%d', a.date,'unixepoch') = STRFTIME('%Y-%m-%d', b.date,'unixepoch')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date AND :beforeDateInclusive >= a.date GROUP BY b.periodic_movement_id ORDER BY groupDate DESC, b.date DESC"
    )
    fun getPeriodicMovementsGroupByDay(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<PeriodicMovementAndCategory>>>


    // move to sunday then move to the first day of the week, if they're equal the events are in the same week
    @Transaction
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.* FROM periodic_movement a" +
                " JOIN periodic_movement b ON STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days')" +
                " = STRFTIME('%Y-%m-%d', b.date,'unixepoch',  'weekday 0', '-' || :weekStartOffset ||' days')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date AND :beforeDateInclusive >= a.date GROUP BY b.periodic_movement_id ORDER BY groupDate DESC, b.date DESC"
    )
    fun getPeriodicMovementsGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate
    ): Flow<Map<GroupInfo, List<PeriodicMovementAndCategory>>>


    @Transaction
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'start of month') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.*FROM periodic_movement a" +
                " JOIN periodic_movement b ON STRFTIME('%Y-%m', a.date,'unixepoch') = STRFTIME('%Y-%m', b.date,'unixepoch')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date AND :beforeDateInclusive >= a.date GROUP BY b.periodic_movement_id ORDER BY groupDate DESC, b.date DESC"
    )
    fun getPeriodicMovementsGroupByMonth(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<PeriodicMovementAndCategory>>>


    @Transaction
    @Query(
        "SELECT '' AS groupDate," +
                " (SELECT SUM(CASE WHEN amount > 0 THEN amount else 0 END) FROM  periodic_movement WHERE date >= :dateFrom AND date <= :dateTo) AS positive," +
                " (SELECT SUM(CASE WHEN amount < 0 THEN amount else 0 END) FROM periodic_movement WHERE date >= :dateFrom AND date <= :dateTo) AS negative," +
                " periodic_movement.* FROM periodic_movement " +
                " JOIN category ON category = name" +
                " WHERE date >= :dateFrom AND date <= :dateTo ORDER BY date DESC"
    )
    fun getPeriodicMovementsInPeriod(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<GroupInfo, List<PeriodicMovementAndCategory>>>


    // move to sunday then move to the first day of the week, if they're equal the events are in the same week
    @Transaction
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.* FROM movement a" +
                " JOIN movement b ON STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days')" +
                " = STRFTIME('%Y-%m-%d', b.date,'unixepoch',  'weekday 0', '-' || :weekStartOffset ||' days')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date AND :beforeDateInclusive >= a.date GROUP BY b.movement_id ORDER BY groupDate DESC, b.date DESC"
    )
    fun getMovementsGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate
    ): Flow<Map<GroupInfo, List<MovementAndCategory>>>


    // move to sunday then move to the first day of the week, if they're equal the events are in the same week
    @Transaction
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days') AS groupDate," +
                " SUM(CASE WHEN a.amount > 0 THEN a.amount else 0 END) AS positive," +
                " SUM(CASE WHEN a.amount < 0 THEN a.amount else 0 END) AS negative, b.* FROM incoming_movement a" +
                " JOIN incoming_movement b ON STRFTIME('%Y-%m-%d', a.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days')" +
                " = STRFTIME('%Y-%m-%d', b.date,'unixepoch',  'weekday 0', '-' || :weekStartOffset ||' days')" +
                " JOIN category ON b.category = category.name WHERE :beforeDateInclusive >= b.date GROUP BY b.incoming_movement_id ORDER BY groupDate ASC, b.date ASC"
    )
    fun getIncomingMovementsGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate
    ): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>>


    @Transaction
    @Query(
        "SELECT '' AS groupDate," +
                " (SELECT SUM(CASE WHEN amount > 0 THEN amount else 0 END) FROM  movement WHERE date >= :dateFrom AND date <= :dateTo) AS positive," +
                " (SELECT SUM(CASE WHEN amount < 0 THEN amount else 0 END) FROM movement WHERE date >= :dateFrom AND date <= :dateTo) AS negative," +
                " movement.* FROM movement " +
                " JOIN category ON category = name" +
                " WHERE date >= :dateFrom AND date <= :dateTo ORDER BY date DESC"
    )
    fun getMovementsInPeriod(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<GroupInfo, List<MovementAndCategory>>>


    @Transaction
    @Query(
        "SELECT '' AS groupDate," +
                " (SELECT SUM(CASE WHEN amount > 0 THEN amount else 0 END) FROM  movement WHERE date >= :dateFrom AND date <= :dateTo) AS positive," +
                " (SELECT SUM(CASE WHEN amount < 0 THEN amount else 0 END) FROM movement WHERE date >= :dateFrom AND date <= :dateTo) AS negative," +
                " incoming_movement.* FROM incoming_movement " +
                " JOIN category ON category = name" +
                " WHERE date >= :dateFrom AND date <= :dateTo ORDER BY date ASC"
    )
    fun getIncomingMovementsInPeriod(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>>

    @Query(
        "SELECT STRFTIME('%Y-%m-%d', movement.date,'unixepoch', 'start of month') AS groupDate," +
                " SUM(CASE WHEN movement.amount > 0 THEN movement.amount else 0 END) AS positive," +
                " SUM(CASE WHEN movement.amount < 0 THEN movement.amount else 0 END) AS negative FROM movement" +
                " WHERE :beforeDateInclusive >= movement.date GROUP BY groupDate ORDER BY groupDate DESC LIMIT :limitData"
    )
    fun getMovementsSumGroupByMonth(
        beforeDateInclusive: LocalDate,
        limitData: Int
    ): Flow<List<GroupInfo>>


    // move to sunday then move to the first day of the week, if they're equal the events are in the same week
    @Query(
        "SELECT STRFTIME('%Y-%m-%d', movement.date,'unixepoch', 'weekday 0', '-' || :weekStartOffset ||' days') AS groupDate," +
                " SUM(CASE WHEN movement.amount > 0 THEN movement.amount else 0 END) AS positive," +
                " SUM(CASE WHEN movement.amount < 0 THEN movement.amount else 0 END) AS negative FROM movement" +
                " WHERE :beforeDateInclusive >= movement.date GROUP BY groupDate ORDER BY groupDate DESC LIMIT :limitData"
    )
    fun getMovementsSumGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate,
        limitData: Int
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
                " GROUP BY STRFTIME('%Y-%m', movement.date, 'unixepoch'), category ORDER BY category LIMIT :limitData"
    )
    fun getCategoriesMovementsMonth(
        categories: List<String>,
        beforeDateInclusive: LocalDate,
        limitData: Int
    ): Flow<Map<Category, List<AmountWithDate>>>

    @Query(
        "SELECT category.*, SUM(CASE WHEN amount < 0 THEN amount else 0 END) AS expense, " +
                "SUM(CASE WHEN amount > 0 THEN amount else 0 END) AS gain, movement.date AS amountDate " +
                "FROM category JOIN movement ON name = movement.category WHERE name IN (:categories)" +
                " GROUP BY STRFTIME('%Y-%m-%d', movement.date,'unixepoch',  'weekday 0', '-' || :weekStartOffset ||' days'), category ORDER BY category LIMIT :limitData"
    )
    fun getCategoriesMovementsWeek(
        categories: List<String>,
        weekStartOffset: Int,
        limitData: Int,
    ): Flow<Map<Category, List<AmountWithDate>>>

    @Query(
        "SELECT category.*, SUM(CASE WHEN amount < 0 THEN amount else 0 END) AS expense, " +
                "SUM(CASE WHEN amount > 0 THEN amount else 0 END) AS gain, movement.date AS amountDate FROM category" +
                " LEFT JOIN movement ON name = movement.category WHERE name IN (:categories)" +
                " AND movement.date >= :dateFrom AND movement.date <= :dateTo" +
                " GROUP BY movement.date, category ORDER BY category"
    )
    fun getCategoriesMovementsPeriod(
        categories: List<String>,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<Category, List<AmountWithDate>>>

    /*
    LEFT JOIN in order to return category data with some null columns if there's no movement
    in the selected period
     */
    @Query(
        "SELECT category.*, positive, negative FROM category LEFT JOIN " +
                "(SELECT movement.category AS catName, SUM(CASE WHEN amount > 0 THEN amount else 0 END) AS positive," +
                " SUM(CASE WHEN amount < 0 THEN amount else 0 END) AS negative FROM" +
                " movement WHERE date >= :dateFrom AND date <= :dateTo GROUP BY catName)" +
                " ON category.name = catName ORDER BY category.name"
    )
    fun getCategoryProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<Category, PositiveNegativeSums>>

    @Query(
        "SELECT SUM(partialSum) AS value FROM (" +
                "SELECT SUM(movement.amount) AS partialSum FROM movement WHERE movement.date >= :dateFrom AND movement.date <= :dateTo" +
                " UNION ALL " +
                "SELECT SUM(incoming_movement.amount) AS partialSum FROM incoming_movement WHERE incoming_movement.periodic_movement_id != NULL AND incoming_movement.date >= :dateFrom AND :dateTo >= incoming_movement.date" +
                ")"
    )
    fun getExpectedSum(dateFrom: LocalDate, dateTo: LocalDate): Flow<Float?>

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
        periodicMovementId: Long,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Movement?

    @Query(
        "SELECT * FROM incoming_movement WHERE periodic_movement_id = :periodicMovementId AND :dateTo >= date AND date >= :dateFrom ORDER BY date DESC LIMIT 1"
    )
    suspend fun getLatestIncomingPeriodic(
        periodicMovementId: Long,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): IncomingMovement?

    @Query(
        "SELECT * FROM periodic_movement WHERE category = :categoryName"
    )
    suspend fun getPeriodicMovements(categoryName: String): List<PeriodicMovement>

    @Query(
        "SELECT * FROM incoming_movement WHERE category = :categoryName"
    )
    suspend fun getIncomingMovements(categoryName: String): List<IncomingMovement>

    @Query(
        "SELECT * FROM incoming_movement WHERE periodic_movement_id = :periodicMovementId"
    )
    suspend fun getAllIncomingFromPeriodic(periodicMovementId: Long): List<IncomingMovement>

    @Query(
        "SELECT * FROM category WHERE name = :name"
    )
    suspend fun getCategoryByName(name: String): Category?
}