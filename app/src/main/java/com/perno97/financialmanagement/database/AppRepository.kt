package com.perno97.financialmanagement.database

import android.util.Log
import androidx.annotation.WorkerThread
import com.perno97.financialmanagement.viewmodels.PositiveNegativeSums
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class AppRepository(private val applicationDao: ApplicationDao) {
    private val logTag = "AppRepository"

    /*
        Getters without parameters
         */
    val allCategories: Flow<List<Category>> = applicationDao.getAllCategories()
    val availableDailyBudget: Flow<Float> = applicationDao.getAvailableDailyBudget()

    @WorkerThread
    suspend fun getCategoryWithMovements(): List<CategoryWithMovements> {
        Log.e(logTag, "Getting categories with movements")
        val toreturn = applicationDao.getCategoryWithMovements()
        Log.e(logTag, "Returning $toreturn value")
        return toreturn
    }

    @WorkerThread
    suspend fun getAllPeriodicMovements(): List<PeriodicMovement> {
        return applicationDao.getAllPeriodicMovements()
    }

    @WorkerThread
    suspend fun getLatestPeriodicMovement(
        periodicMovementId: Int,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Movement? {
        return applicationDao.getLatestPeriodicMovement(periodicMovementId, dateFrom, dateTo)
    }

    @WorkerThread
    suspend fun getLatestIncomingPeriodic(
        periodicMovementId: Int,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): IncomingMovement? {
        return applicationDao.getLatestIncomingPeriodic(periodicMovementId, dateFrom, dateTo)
    }

    @WorkerThread
    suspend fun getPeriodicMovements(categoryName: String): List<PeriodicMovement> {
        return applicationDao.getPeriodicMovements(categoryName)
    }

    @WorkerThread
    suspend fun getIncomingMovements(categoryName: String): List<IncomingMovement> {
        return applicationDao.getIncomingMovements(categoryName)
    }

    @WorkerThread
    suspend fun getAllIncomingFromPeriodic(periodicMovementId: Int): List<IncomingMovement> {
        return applicationDao.getAllIncomingFromPeriodic(periodicMovementId)
    }


    /*
    Getters with parameters
     */
    @WorkerThread
    fun getMovementsGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate
    ): Flow<Map<GroupInfo, List<MovementAndCategory>>> {
        return applicationDao.getMovementsGroupByWeek(weekStartOffset, beforeDateInclusive)
    }

    @WorkerThread
    fun getIncomingMovementsGroupByWeek(weekStartOffset: Int): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>> {
        return applicationDao.getIncomingMovementsGroupByWeek(weekStartOffset)
    }

    @WorkerThread
    fun getMovementsGroupByDay(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<MovementAndCategory>>> =
        applicationDao.getMovementsGroupByDay(beforeDateInclusive)

    @WorkerThread
    fun getIncomingMovementsGroupByDay(): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>> =
        applicationDao.getIncomingMovementsGroupByDay()

    @WorkerThread
    fun getMovementsGroupByMonth(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<MovementAndCategory>>> =
        applicationDao.getMovementsGroupByMonth(beforeDateInclusive)

    @WorkerThread
    fun getIncomingMovementsGroupByMonth(): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>> =
        applicationDao.getIncomingMovementsGroupByMonth()

    @WorkerThread
    fun getMovementsInPeriod(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<GroupInfo, List<MovementAndCategory>>> {
        return applicationDao.getMovementsInPeriod(dateFrom, dateTo)
    }

    @WorkerThread
    fun getIncomingMovementsInPeriod(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>> {
        return applicationDao.getIncomingMovementsInPeriod(dateFrom, dateTo)
    }

    @WorkerThread
    fun getProfile(profileId: Int): Flow<Profile> {
        return applicationDao.getProfile(profileId)
    }

    @WorkerThread
    fun countIncoming(beforeDateInclusive: LocalDate): Flow<Int> {
        return applicationDao.countIncoming(beforeDateInclusive)
    }

    @WorkerThread
    suspend fun getCurrentAsset(defaultProfileId: Int): Float {
        return applicationDao.getCurrentAsset(defaultProfileId)
    }

    @WorkerThread
    fun getCategory(categoryName: String): Flow<Category> {
        return applicationDao.getCategory(categoryName)
    }

    @WorkerThread
    fun getCategoryExpensesProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<Category, Expense>> {
        return applicationDao.getCategoryExpensesProgresses(dateFrom, dateTo)
    }

    @WorkerThread
    fun getCategoriesExpensesMonth(
        categories: List<String>,
        beforeDateInclusive: LocalDate
    ): Flow<Map<Category, List<AmountWithDate>>> {
        return applicationDao.getCategoriesExpensesMonth(categories, beforeDateInclusive)
    }

    @WorkerThread
    fun getCategoriesExpensesWeek(
        categories: List<String>,
        weekStartOffset: Int
    ): Flow<Map<Category, List<AmountWithDate>>> {
        return applicationDao.getCategoriesExpensesWeek(categories, weekStartOffset)
    }

    @WorkerThread
    fun getCategoriesExpensesPeriod(
        categories: List<String>,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<Category, List<AmountWithDate>>> {
        return applicationDao.getCategoriesExpensesPeriod(categories, dateFrom, dateTo)
    }

    @WorkerThread
    fun getCategoryProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<Category, PositiveNegativeSums>> {
        return applicationDao.getCategoryProgresses(dateFrom, dateTo)
    }

    @WorkerThread
    fun getExpectedSum(dateFrom: LocalDate, dateTo: LocalDate): Flow<Float?> {
        return applicationDao.getExpectedSum(dateFrom, dateTo)
    }

    @WorkerThread
    fun getMovementsSumGroupByMonth(beforeDateInclusive: LocalDate): Flow<List<GroupInfo>> {
        return applicationDao.getMovementsSumGroupByMonth(beforeDateInclusive)
    }

    @WorkerThread
    fun getMovementsSumGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate
    ): Flow<List<GroupInfo>> {
        return applicationDao.getMovementsSumGroupByWeek(
            weekStartOffset,
            beforeDateInclusive
        )
    }

    @WorkerThread
    fun getMovementsSumInPeriod(dateFrom: LocalDate, dateTo: LocalDate): Flow<List<GroupInfo>> {
        return applicationDao.getMovementsSumInPeriod(dateFrom, dateTo)
    }

    @WorkerThread
    fun getGainsAndExpensesInPeriod(dateFrom: LocalDate, dateTo: LocalDate): Flow<GroupInfo> {
        return applicationDao.getGainsAndExpensesInPeriod(dateFrom, dateTo)
    }


    /*
    Insert
     */
    @WorkerThread
    suspend fun insert(category: Category) {
        applicationDao.insertCategories(category)
    }

    @WorkerThread
    suspend fun insert(movement: Movement) {
        applicationDao.insertMovements(movement)
    }

    @WorkerThread
    suspend fun insert(periodicMovement: PeriodicMovement) {
        applicationDao.insertPeriodicMovements(periodicMovement)
    }

    @WorkerThread
    suspend fun insert(incomingMovement: IncomingMovement): Long {
        return applicationDao.insertIncumbentMovement(incomingMovement)
    }

    @WorkerThread
    suspend fun insert(profile: Profile) {
        applicationDao.insertProfiles(profile)
    }


    /*
    Update
     */
    @WorkerThread
    suspend fun update(movement: Movement) {
        applicationDao.updateMovements(movement)
    }

    @WorkerThread
    suspend fun update(category: Category) {
        applicationDao.updateCategories(category)
    }

    @WorkerThread
    suspend fun update(incomingMovement: IncomingMovement) {
        applicationDao.updateIncumbentMovements(incomingMovement)
    }

    @WorkerThread
    suspend fun update(periodicMovement: PeriodicMovement) {
        applicationDao.updatePeriodicMovements(periodicMovement)
    }

    @WorkerThread
    suspend fun updateAssets(profileId: Int, assetsValue: Float) {
        applicationDao.updateAssets(profileId, assetsValue)
    }


    /*
    Delete
     */
    @WorkerThread
    suspend fun delete(category: Category) {
        applicationDao.deleteCategory(category)
    }

    @WorkerThread
    suspend fun deleteMovementFromId(movementId: Int) {
        applicationDao.deleteMovementFromId(movementId)
    }

    @WorkerThread
    suspend fun deleteIncomingMovementFromId(incomingMovementId: Int) {
        applicationDao.deleteIncumbentMovementFromId(incomingMovementId)
    }

    @WorkerThread
    suspend fun deletePeriodicMovement(periodicMovementId: Int) {
        applicationDao.deletePeriodicMovementFromId(periodicMovementId)
    }
}