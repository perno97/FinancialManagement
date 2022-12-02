package com.perno97.financialmanagement.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class AppRepository(private val applicationDao: ApplicationDao) {

    val allCategories: Flow<List<Category>> = applicationDao.getAllCategories()

    val allMovements: Flow<List<Movement>> = applicationDao.getAllMovements()

    val movementsGroupByMonth: Flow<Map<GroupInfo, List<MovementAndCategory>>> =
        applicationDao.getMovementsGroupByMonth()

    val movementAndCategory: Flow<List<MovementAndCategory>> =
        applicationDao.getMovementAndCategory()

    @WorkerThread
    suspend fun insert(category: Category) {
        applicationDao.insertAllCategories(category)
    }

    @WorkerThread
    suspend fun insert(movement: Movement) {
        applicationDao.insertAllMovements(movement)
    }

    @WorkerThread
    suspend fun deleteAllCategories() {
        applicationDao.deleteAllCategories()
    }

    @WorkerThread
    fun getCategoryBudgetsList(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Flow<List<CategoryWithExpensesSum>> {
        return applicationDao.getCategoryBudgetsList(dateFrom, dateTo)
    }
}