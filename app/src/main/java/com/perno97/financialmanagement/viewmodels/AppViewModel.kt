package com.perno97.financialmanagement.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.perno97.financialmanagement.utils.PeriodState
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.core.util.Pair
import com.perno97.financialmanagement.database.*
import kotlinx.coroutines.flow.*
import kotlin.math.log

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    private val logTag = "AppViewModel"

    val defaultProfileId = 0
    private val _uiState = MutableStateFlow(FinancialManagementUiState())

    /*private val _categoryFiltersState: MutableLiveData<CategoryFiltersUiState> by lazy {
        MutableLiveData<CategoryFiltersUiState>(CategoryFiltersUiState())
    }*/
    //val categoryFiltersState: LiveData<CategoryFiltersUiState> = _categoryFiltersState
    val uiState: StateFlow<FinancialManagementUiState> = _uiState.asStateFlow()

    fun setMainPeriod(
        from: LocalDate,
        to: LocalDate,
        state: PeriodState,
        datePickerSelection: Pair<Long, Long>?
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                dateFromMain = from,
                dateToMain = to,
                stateMain = state,
                datePickerSelectionMain = datePickerSelection
            )
        }
    }

    fun setCatDetailsPeriod(
        from: LocalDate,
        to: LocalDate,
        state: PeriodState,
        datePickerSelection: Pair<Long, Long>?
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                dateFromCatDetails = from,
                dateToCatDetails = to,
                stateCatDetails = state,
                datePickerSelectionCatDetails = datePickerSelection
            )
        }
    }

    fun setCategoryFilters(filters: List<Category>) {
        _uiState.update { currentState ->
            currentState.copy(
                categoryFilters = filters
            )
        }
    }

    val allCategories: LiveData<List<Category>> = repository.allCategories.asLiveData()

    val allMovements: LiveData<List<Movement>> = repository.allMovements.asLiveData()

    val availableDailyBudget: LiveData<Float> = repository.availableDailyBudget.asLiveData()

    val movementsGroupByMonth: LiveData<Map<GroupInfo, List<MovementAndCategory>>> =
        repository.movementsGroupByMonth.asLiveData()

    val movementsGroupByDay: LiveData<Map<GroupInfo, List<MovementAndCategory>>> =
        repository.movementsGroupByDay.asLiveData()

    val allMovementsAndCategories: LiveData<List<MovementAndCategory>> =
        repository.movementAndCategory.asLiveData()

    fun insert(category: Category) = viewModelScope.launch {
        repository.insert(category)
    }

    fun insert(movement: Movement) = viewModelScope.launch {
        repository.insert(movement)
    }

    fun insertDefaultProfile(assetValue: Float) = viewModelScope.launch {
        repository.insert(Profile(defaultProfileId, assetValue))
    }

    fun getMovementsGroupByWeek(weekStartOffset: Int): LiveData<Map<GroupInfo, List<MovementAndCategory>>> {
        return repository.getMovementsGroupByWeek(weekStartOffset).asLiveData()
    }

    fun getMovementsInPeriod(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): LiveData<Map<GroupInfo, List<MovementAndCategory>>> {
        return repository.getMovementsInPeriod(dateFrom, dateTo).asLiveData()
    }

    fun getDefaultProfile(): LiveData<Profile> {
        return repository.getProfile(defaultProfileId).asLiveData()
    }

    fun getCategory(categoryName: String): LiveData<Category> {
        return repository.getCategory(categoryName).asLiveData()
    }

    fun getCategoryExpensesProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): LiveData<Map<Category, Expense>> {
        return repository.getCategoryExpensesProgresses(dateFrom, dateTo).asLiveData()
    }

    fun getCategoryExpensesProgress(
        dateFrom: LocalDate,
        dateTo: LocalDate,
        categoryName: String
    ): LiveData<Expense> {
        return repository.getCategoryExpensesProgress(dateFrom, dateTo, categoryName).asLiveData()
    }
}