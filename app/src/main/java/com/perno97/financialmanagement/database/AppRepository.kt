package com.perno97.financialmanagement.database

import androidx.annotation.WorkerThread
import com.perno97.financialmanagement.viewmodels.PositiveNegativeSums
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class AppRepository(private val applicationDao: ApplicationDao) {

    /*
        Getters without parameters
         */
    val allCategories: Flow<List<Category>> = applicationDao.getAllCategories()

    @WorkerThread
    suspend fun getCategoryWithMovements(): List<CategoryWithMovements> {
        return applicationDao.getCategoryWithMovements()
    }

    @WorkerThread
    suspend fun getAllPeriodicMovements(): List<PeriodicMovement> {
        return applicationDao.getAllPeriodicMovements()
    }

    @WorkerThread
    suspend fun getLatestIncomingPeriodic(
        periodicMovementId: Long,
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
    suspend fun getAllIncomingFromPeriodic(periodicMovementId: Long): List<IncomingMovement> {
        return applicationDao.getAllIncomingFromPeriodic(periodicMovementId)
    }

    @WorkerThread
    suspend fun getCategoryByName(name: String): Category? {
        return applicationDao.getCategoryByName(name)
    }


    /*
    Getters with parameters
     */
    @WorkerThread
    suspend fun getPeriodicMovement(periodicMovementId: Long): PeriodicMovement? {
        return applicationDao.getPeriodicMovement(periodicMovementId)
    }

    @WorkerThread
    fun getMovementsGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate
    ): Flow<Map<GroupInfo, List<MovementAndCategory>>> {
        return applicationDao.getMovementsGroupByWeek(weekStartOffset, beforeDateInclusive)
    }

    @WorkerThread
    fun getIncomingMovementsGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate
    ): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>> {
        return applicationDao.getIncomingMovementsGroupByWeek(weekStartOffset, beforeDateInclusive)
    }

    @WorkerThread
    fun getMovementsGroupByDay(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<MovementAndCategory>>> =
        applicationDao.getMovementsGroupByDay(beforeDateInclusive)

    @WorkerThread
    fun getIncomingMovementsGroupByDay(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>> =
        applicationDao.getIncomingMovementsGroupByDay(beforeDateInclusive)

    @WorkerThread
    fun getPeriodicMovementsGroupByDay(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<PeriodicMovementAndCategory>>> =
        applicationDao.getPeriodicMovementsGroupByDay(beforeDateInclusive)

    @WorkerThread
    fun getPeriodicMovementsGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate
    ): Flow<Map<GroupInfo, List<PeriodicMovementAndCategory>>> =
        applicationDao.getPeriodicMovementsGroupByWeek(weekStartOffset, beforeDateInclusive)

    @WorkerThread
    fun getPeriodicMovementsGroupByMonth(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<PeriodicMovementAndCategory>>> =
        applicationDao.getPeriodicMovementsGroupByMonth(beforeDateInclusive)

    @WorkerThread
    fun getPeriodicMovementsInPeriod(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<GroupInfo, List<PeriodicMovementAndCategory>>> =
        applicationDao.getPeriodicMovementsInPeriod(dateFrom, dateTo)

    @WorkerThread
    fun getMovementsGroupByMonth(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<MovementAndCategory>>> =
        applicationDao.getMovementsGroupByMonth(beforeDateInclusive)

    @WorkerThread
    fun getIncomingMovementsGroupByMonth(beforeDateInclusive: LocalDate): Flow<Map<GroupInfo, List<IncomingMovementAndCategory>>> =
        applicationDao.getIncomingMovementsGroupByMonth(beforeDateInclusive)

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
    fun getProfile(profileId: Long): Flow<Profile> {
        return applicationDao.getProfile(profileId)
    }

    @WorkerThread
    fun countIncoming(beforeDateInclusive: LocalDate): Flow<Int> {
        return applicationDao.countIncoming(beforeDateInclusive)
    }

    @WorkerThread
    suspend fun getCurrentAsset(profileId: Long): Float {
        return applicationDao.getCurrentAsset(profileId)
    }

    @WorkerThread
    suspend fun getLastAccess(profileId: Long): LocalDate? {
        return applicationDao.getLastAccess(profileId)
    }

    @WorkerThread
    fun getCategory(categoryId: Long): Flow<Category> {
        return applicationDao.getCategory(categoryId)
    }

    @WorkerThread
    fun getCategoryExpensesProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<Category, Expense>> {
        return applicationDao.getCategoryExpensesProgresses(dateFrom, dateTo)
    }

    @WorkerThread
    fun getCategoriesMovementsMonth(
        categories: List<String>,
        beforeDateInclusive: LocalDate,
        limitData: Int
    ): Flow<Map<Category, List<AmountWithDate>>> {
        return applicationDao.getCategoriesMovementsMonth(
            categories,
            beforeDateInclusive,
            limitData
        )
    }

    @WorkerThread
    fun getCategoriesMovementsWeek(
        categories: List<String>,
        weekStartOffset: Int,
        limitData: Int
    ): Flow<Map<Category, List<AmountWithDate>>> {
        return applicationDao.getCategoriesMovementsWeek(categories, weekStartOffset, limitData)
    }

    @WorkerThread
    fun getCategoriesMovementsPeriod(
        categories: List<String>,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<Category, List<AmountWithDate>>> {
        return applicationDao.getCategoriesMovementsPeriod(categories, dateFrom, dateTo)
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
    fun getMovementsSumGroupByMonth(
        beforeDateInclusive: LocalDate,
        limitData: Int
    ): Flow<List<GroupInfo>> {
        return applicationDao.getMovementsSumGroupByMonth(beforeDateInclusive, limitData)
    }

    @WorkerThread
    fun getMovementsSumGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate,
        limitData: Int
    ): Flow<List<GroupInfo>> {
        return applicationDao.getMovementsSumGroupByWeek(
            weekStartOffset,
            beforeDateInclusive,
            limitData
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
    suspend fun insert(periodicMovement: PeriodicMovement): Long {
        return applicationDao.insertPeriodicMovement(periodicMovement)
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
        applicationDao.updateIncomingMovements(incomingMovement)
    }

    @WorkerThread
    suspend fun update(periodicMovement: PeriodicMovement) {
        applicationDao.updatePeriodicMovements(periodicMovement)
    }

    @WorkerThread
    suspend fun updateAssets(profileId: Long, assetsValue: Float) {
        applicationDao.updateAssets(profileId, assetsValue)
    }

    @WorkerThread
    suspend fun updateCategoryNameInMovements(oldName: String, newName: String) {
        applicationDao.updateCategoryNameInMovements(oldName, newName)
    }

    @WorkerThread
    suspend fun updateLastAccess(profileId: Long, lastAccess: LocalDate) {
        applicationDao.updateLastAccess(profileId, lastAccess)
    }


    /*
    Delete
     */
    @WorkerThread
    suspend fun delete(category: Category) {
        applicationDao.deleteCategory(category)
    }

    @WorkerThread
    suspend fun deleteMovementFromId(movementId: Long) {
        applicationDao.deleteMovementFromId(movementId)
    }

    @WorkerThread
    suspend fun deleteIncomingMovementFromId(incomingMovementId: Long) {
        applicationDao.deleteIncumbentMovementFromId(incomingMovementId)
    }

    @WorkerThread
    suspend fun deletePeriodicMovement(periodicMovementId: Long) {
        applicationDao.deletePeriodicMovementFromId(periodicMovementId)
    }

    @WorkerThread
    suspend fun deleteAllIncomingOfPeriodic(periodicMovementId: Long) {
        applicationDao.deleteAllIncomingOfPeriodic(periodicMovementId)
    }
}