package com.perno97.financialmanagement.viewmodels

import androidx.lifecycle.*
import com.perno97.financialmanagement.utils.PeriodState
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.core.util.Pair
import com.perno97.financialmanagement.database.*
import kotlinx.coroutines.flow.*

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    private val logTag = "AppViewModel"

    val defaultProfileId = 0
    private val _uiState = MutableStateFlow(FinancialManagementUiState())
    val uiState: StateFlow<FinancialManagementUiState> = _uiState.asStateFlow()


    /*
    Getters without parameters
     */
    val allCategories: LiveData<List<Category>> = repository.allCategories.asLiveData()
    val allMovements: LiveData<List<Movement>> = repository.allMovements.asLiveData()
    val availableDailyBudget: LiveData<Float> = repository.availableDailyBudget.asLiveData()
    val movementsGroupByMonth: LiveData<Map<GroupInfo, List<MovementAndCategory>>> =
        repository.movementsGroupByMonth.asLiveData()
    val movementsGroupByDay: LiveData<Map<GroupInfo, List<MovementAndCategory>>> =
        repository.movementsGroupByDay.asLiveData()
    val allMovementsAndCategories: LiveData<List<MovementAndCategory>> =
        repository.movementAndCategory.asLiveData()
    val categoryWithMovements: LiveData<List<CategoryWithMovements>> =
        repository.categoryWithMovements.asLiveData()

    fun getDefaultProfile(): LiveData<Profile> {
        return repository.getProfile(defaultProfileId).asLiveData()
    }


    /*
    Getters with parameters
     */
    fun getMovementsGroupByWeek(weekStartOffset: Int): LiveData<Map<GroupInfo, List<MovementAndCategory>>> {
        return repository.getMovementsGroupByWeek(weekStartOffset).asLiveData()
    }

    fun getMovementsInPeriod(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): LiveData<Map<GroupInfo, List<MovementAndCategory>>> {
        return repository.getMovementsInPeriod(dateFrom, dateTo).asLiveData()
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

    fun getCategoriesExpensesMonth(categories: List<String>): LiveData<Map<Category, List<AmountWithDate>>> {
        return repository.getCategoriesExpensesMonth(categories).asLiveData()
    }

    fun getCategoryProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): LiveData<Map<Category, PositiveNegativeSums>> {
        return repository.getCategoryProgresses(dateFrom, dateTo).asLiveData()
    }


    /*
    UI state related
     */
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


    /*
    Insert
     */
    fun insert(category: Category) = viewModelScope.launch {
        repository.insert(category)
    }

    fun insert(movement: Movement) = viewModelScope.launch {
        repository.insert(movement)
    }

    fun insertDefaultProfile(assetValue: Float) = viewModelScope.launch {
        repository.insert(Profile(defaultProfileId, assetValue))
    }


    /*
    Delete
     */
    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.delete(category)
    }
}