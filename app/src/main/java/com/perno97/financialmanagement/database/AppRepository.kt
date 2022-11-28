package com.perno97.financialmanagement.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class AppRepository(private val applicationDao: ApplicationDao) {
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
    fun getCategoryBudgetsList(dateFrom: LocalDate, dateTo: LocalDate): Flow<List<CategoryWithExpensesSum>> {
        return applicationDao.getCategoryBudgetsList(dateFrom, dateTo)
    }
}