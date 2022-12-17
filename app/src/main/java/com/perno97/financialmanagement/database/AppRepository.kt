package com.perno97.financialmanagement.database

import android.util.Log
import androidx.annotation.WorkerThread
import com.perno97.financialmanagement.viewmodels.PositiveNegativeSums
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import kotlin.math.log

class AppRepository(private val applicationDao: ApplicationDao) {
    private val logTag = "AppRepository"

    /*
        Getters without parameters
         */
    val allCategories: Flow<List<Category>> = applicationDao.getAllCategories()
    val allMovements: Flow<List<Movement>> = applicationDao.getAllMovements()
    val availableDailyBudget: Flow<Float> = applicationDao.getAvailableDailyBudget()
    val movementsGroupByMonth: Flow<Map<GroupInfo, List<MovementAndCategory>>> =
        applicationDao.getMovementsGroupByMonth()
    val movementsGroupByDay: Flow<Map<GroupInfo, List<MovementAndCategory>>> =
        applicationDao.getMovementsGroupByDay()
    val movementAndCategory: Flow<List<MovementAndCategory>> =
        applicationDao.getMovementAndCategory()

    suspend fun getCategoryWithMovements(): List<CategoryWithMovements> {
        Log.e(logTag, "Getting categories with movements")
        val toreturn = applicationDao.getCategoryWithMovements()
        Log.e(logTag, "Returning ${toreturn} value")
        return toreturn
    }


    /*
    Getters with parameters
     */
    @WorkerThread
    fun getMovementsGroupByWeek(weekStartOffset: Int): Flow<Map<GroupInfo, List<MovementAndCategory>>> {
        return applicationDao.getMovementsGroupByWeek(weekStartOffset)
    }

    @WorkerThread
    fun getMovementsInPeriod(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<GroupInfo, List<MovementAndCategory>>> {
        return applicationDao.getMovementsInPeriod(dateFrom, dateTo)
    }

    @WorkerThread
    fun getProfile(profileId: Int): Flow<Profile> {
        return applicationDao.getProfile(profileId)
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
    fun getCategoryExpensesProgress(
        dateFrom: LocalDate,
        dateTo: LocalDate,
        categoryName: String
    ): Flow<Expense> {
        return applicationDao.getCategoryExpensesProgress(dateFrom, dateTo, categoryName)
    }

    @WorkerThread
    fun getCategoriesExpensesMonth(categories: List<String>): Flow<Map<Category, List<AmountWithDate>>> {
        return applicationDao.getCategoriesExpensesMonth(categories)
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
    suspend fun insert(profile: Profile) {
        applicationDao.insertProfiles(profile)
    }


    /*
    Delete
     */
    @WorkerThread
    suspend fun delete(category: Category) {
        applicationDao.deleteCategory(category)
    }
}