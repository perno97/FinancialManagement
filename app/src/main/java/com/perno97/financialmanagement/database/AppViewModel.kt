package com.perno97.financialmanagement.database

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    val allCategories: LiveData<List<Category>> = repository.allCategories.asLiveData()

    val allMovements: LiveData<List<Movement>> = repository.allMovements.asLiveData()

    val movementsGroupByMonth: LiveData<Map<GroupInfo, List<MovementAndCategory>>> =
        repository.movementsGroupByMonth.asLiveData()

    val allMovementsAndCategories: LiveData<List<MovementAndCategory>> =
        repository.movementAndCategory.asLiveData()

    fun insert(category: Category) = viewModelScope.launch {
        repository.insert(category)
    }

    fun insert(movement: Movement) = viewModelScope.launch {
        repository.insert(movement)
    }

    fun deleteAllCategories() = viewModelScope.launch {
        repository.deleteAllCategories()
    }

    fun getCategoryBudgetsList(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): LiveData<List<CategoryWithExpensesSum>> {
        return repository.getCategoryBudgetsList(dateFrom, dateTo).asLiveData()
    }
}

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}