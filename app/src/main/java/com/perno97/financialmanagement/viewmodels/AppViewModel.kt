package com.perno97.financialmanagement.viewmodels

import android.util.Log
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
    val allMovementsAndCategories: LiveData<List<MovementAndCategory>> =
        repository.movementAndCategory.asLiveData()

    fun getDefaultProfile(): LiveData<Profile> {
        return repository.getProfile(defaultProfileId).asLiveData()
    }

    suspend fun getCategoryWithMovements(): List<CategoryWithMovements> {
        Log.e(logTag, "Getting categories with movements")
        val toreturn = repository.getCategoryWithMovements()
        Log.e(logTag, "Returning $toreturn value")
        return toreturn
    }

    suspend fun getCurrentAssetDefault(): Float {
        return repository.getCurrentAsset(defaultProfileId)
    }


    /*
    Getters with parameters
     */
    fun getMovementsGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate
    ): LiveData<Map<GroupInfo, List<MovementAndCategory>>> {
        return repository.getMovementsGroupByWeek(weekStartOffset, beforeDateInclusive).asLiveData()
    }

    fun getMovementsGroupByDay(beforeDateInclusive: LocalDate): LiveData<Map<GroupInfo, List<MovementAndCategory>>> {
        return repository.getMovementsGroupByDay(beforeDateInclusive).asLiveData()
    }

    fun movementsGroupByMonth(beforeDateInclusive: LocalDate): LiveData<Map<GroupInfo, List<MovementAndCategory>>> {
        return repository.getMovementsGroupByMonth(beforeDateInclusive).asLiveData()
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

    fun getCategoriesExpensesMonth(categories: List<String>, beforeDateInclusive: LocalDate): LiveData<Map<Category, List<AmountWithDate>>> {
        return repository.getCategoriesExpensesMonth(categories, beforeDateInclusive).asLiveData()
    }

    fun getCategoriesExpensesWeek(
        categories: List<String>,
        weekStartOffset: Int
    ): LiveData<Map<Category, List<AmountWithDate>>> {
        return repository.getCategoriesExpensesWeek(categories, weekStartOffset).asLiveData()
    }

    fun getCategoriesExpensesPeriod(
        categories: List<String>,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): LiveData<Map<Category, List<AmountWithDate>>> {
        return repository.getCategoriesExpensesPeriod(categories, dateFrom, dateTo).asLiveData()
    }

    fun getCategoryProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): LiveData<Map<Category, PositiveNegativeSums>> {
        return repository.getCategoryProgresses(dateFrom, dateTo).asLiveData()
    }

    fun getExpectedSum(dateFrom: LocalDate, dateTo: LocalDate): LiveData<Float> {
        return repository.getExpectedSum(dateFrom, dateTo).asLiveData()
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

    fun insertNewAssets(assetValue: Float) = viewModelScope.launch {
        repository.insert(Profile(defaultProfileId, assetValue))
    }


    /*
    Delete
     */
    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.delete(category)
    }
}