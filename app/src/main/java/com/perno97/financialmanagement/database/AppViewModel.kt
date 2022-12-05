package com.perno97.financialmanagement.database

import androidx.lifecycle.*
import com.perno97.financialmanagement.FinancialManagementUiState
import com.perno97.financialmanagement.utils.PeriodState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.core.util.Pair
import java.util.*

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancialManagementUiState())
    val uiState: StateFlow<FinancialManagementUiState> = _uiState.asStateFlow()

    fun setPeriod(
        from: LocalDate,
        to: LocalDate,
        state: PeriodState,
        datePickerSelection: Pair<Long, Long>?
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                dateFrom = from,
                dateTo = to,
                state = state,
                datePickerSelection = datePickerSelection
            )
        }
    }

    val allCategories: LiveData<List<Category>> = repository.allCategories.asLiveData()

    val allMovements: LiveData<List<Movement>> = repository.allMovements.asLiveData()

    val availableDailyBudget: LiveData<Float> = repository.availableDailyBudget.asLiveData()

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

    fun insert(profile: Profile) = viewModelScope.launch {
        repository.insert(profile)
    }

    fun getProfile(profileId: Int): LiveData<Profile> {
        return repository.getProfile(profileId).asLiveData()
    }

    fun getCategoryExpensesProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): LiveData<List<CategoryWithExpensesSum>> {
        return repository.getCategoryExpensesProgresses(dateFrom, dateTo).asLiveData()
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