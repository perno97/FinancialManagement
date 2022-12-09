package com.perno97.financialmanagement.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class AppRepository(private val applicationDao: ApplicationDao) {

    val allCategories: Flow<List<Category>> = applicationDao.getAllCategories()

    val allMovements: Flow<List<Movement>> = applicationDao.getAllMovements()

    val availableDailyBudget: Flow<Float> = applicationDao.getAvailableDailyBudget()

    val movementsGroupByMonth: Flow<Map<GroupInfo, List<MovementAndCategory>>> =
        applicationDao.getMovementsGroupByMonth()

    val movementAndCategory: Flow<List<MovementAndCategory>> =
        applicationDao.getMovementAndCategory()

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

    @WorkerThread
    fun getProfile(profileId: Int): Flow<Profile> {
        return applicationDao.getProfile(profileId)
    }

    @WorkerThread
    fun getCategoryExpensesProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<Map<Category, Expense>> {
        return applicationDao.getCategoryExpensesProgresses(dateFrom, dateTo)
    }
}