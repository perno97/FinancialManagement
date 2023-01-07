package com.perno97.financialmanagement.viewmodels

import androidx.core.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.perno97.financialmanagement.database.*
import com.perno97.financialmanagement.utils.PeriodState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    private val defaultProfileId = 0
    private val _uiState = MutableStateFlow(FinancialManagementUiState())
    val uiState: StateFlow<FinancialManagementUiState> = _uiState.asStateFlow()


    /*
    Getters without parameters
     */
    val allCategories: LiveData<List<Category>> = repository.allCategories.asLiveData()
    val availableDailyBudget: LiveData<Float> = repository.availableDailyBudget.asLiveData()

    fun getDefaultProfile(): LiveData<Profile> {
        return repository.getProfile(defaultProfileId).asLiveData()
    }

    fun countIncoming(beforeDateInclusive: LocalDate): LiveData<Int> {
        return repository.countIncoming(beforeDateInclusive).asLiveData()
    }

    suspend fun getCategoryWithMovements(): List<CategoryWithMovements> {
        return repository.getCategoryWithMovements()
    }

    suspend fun getCurrentAssetDefault(): Float {
        return repository.getCurrentAsset(defaultProfileId)
    }

    suspend fun getAllPeriodicMovements(): List<PeriodicMovement> {
        return repository.getAllPeriodicMovements()
    }

    suspend fun getLatestPeriodicMovement(
        periodicMovementId: Int,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Movement? {
        return repository.getLatestPeriodicMovement(periodicMovementId, dateFrom, dateTo)
    }

    suspend fun getLatestIncomingPeriodic(
        periodicMovementId: Int,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): IncomingMovement? {
        return repository.getLatestIncomingPeriodic(periodicMovementId, dateFrom, dateTo)
    }

    suspend fun getPeriodicMovements(categoryName: String): List<PeriodicMovement> {
        return repository.getPeriodicMovements(categoryName)
    }

    suspend fun getIncomingMovements(categoryName: String): List<IncomingMovement> {
        return repository.getIncomingMovements(categoryName)
    }

    suspend fun getAllIncomingFromPeriodic(periodicMovementId: Int): List<IncomingMovement> {
        return repository.getAllIncomingFromPeriodic(periodicMovementId)
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

    fun getIncomingMovementsGroupByWeek(
        weekStartOffset: Int
    ): LiveData<Map<GroupInfo, List<IncomingMovementAndCategory>>> {
        return repository.getIncomingMovementsGroupByWeek(weekStartOffset).asLiveData()
    }

    fun getMovementsGroupByDay(beforeDateInclusive: LocalDate): LiveData<Map<GroupInfo, List<MovementAndCategory>>> {
        return repository.getMovementsGroupByDay(beforeDateInclusive).asLiveData()
    }

    fun getIncomingMovementsGroupByDay(): LiveData<Map<GroupInfo, List<IncomingMovementAndCategory>>> {
        return repository.getIncomingMovementsGroupByDay().asLiveData()
    }

    fun getPeriodicMovementsGroupByDay(beforeDateInclusive: LocalDate): LiveData<Map<GroupInfo, List<PeriodicMovementAndCategory>>> {
        return repository.getPeriodicMovementsGroupByDay(beforeDateInclusive).asLiveData()
    }

    fun getPeriodicMovementsGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate
    ): LiveData<Map<GroupInfo, List<PeriodicMovementAndCategory>>> {
        return repository.getPeriodicMovementsGroupByWeek(weekStartOffset, beforeDateInclusive)
            .asLiveData()
    }

    fun getPeriodicMovementsGroupByMonth(beforeDateInclusive: LocalDate): LiveData<Map<GroupInfo, List<PeriodicMovementAndCategory>>> {
        return repository.getPeriodicMovementsGroupByMonth(beforeDateInclusive).asLiveData()
    }

    fun getPeriodicMovementsInPeriod(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): LiveData<Map<GroupInfo, List<PeriodicMovementAndCategory>>> {
        return repository.getPeriodicMovementsInPeriod(dateFrom, dateTo).asLiveData()
    }

    fun getMovementsGroupByMonth(beforeDateInclusive: LocalDate): LiveData<Map<GroupInfo, List<MovementAndCategory>>> {
        return repository.getMovementsGroupByMonth(beforeDateInclusive).asLiveData()
    }

    fun getIncomingMovementsGroupByMonth(): LiveData<Map<GroupInfo, List<IncomingMovementAndCategory>>> {
        return repository.getIncomingMovementsGroupByMonth().asLiveData()
    }

    fun getMovementsInPeriod(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): LiveData<Map<GroupInfo, List<MovementAndCategory>>> {
        return repository.getMovementsInPeriod(dateFrom, dateTo).asLiveData()
    }

    fun getIncomingMovementsInPeriod(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): LiveData<Map<GroupInfo, List<IncomingMovementAndCategory>>> {
        return repository.getIncomingMovementsInPeriod(dateFrom, dateTo).asLiveData()
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

    fun getCategoriesMovementsMonth(
        categories: List<String>,
        beforeDateInclusive: LocalDate,
        limitData: Int
    ): LiveData<Map<Category, List<AmountWithDate>>> {
        return repository.getCategoriesMovementsMonth(categories, beforeDateInclusive, limitData)
            .asLiveData()
    }

    fun getCategoriesMovementsWeek(
        categories: List<String>,
        weekStartOffset: Int,
        limitData: Int
    ): LiveData<Map<Category, List<AmountWithDate>>> {
        return repository.getCategoriesMovementsWeek(categories, weekStartOffset, limitData)
            .asLiveData()
    }

    fun getCategoriesMovementsPeriod(
        categories: List<String>,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): LiveData<Map<Category, List<AmountWithDate>>> {
        return repository.getCategoriesMovementsPeriod(categories, dateFrom, dateTo).asLiveData()
    }

    fun getCategoryProgresses(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): LiveData<Map<Category, PositiveNegativeSums>> {
        return repository.getCategoryProgresses(dateFrom, dateTo).asLiveData()
    }

    fun getExpectedSum(dateFrom: LocalDate, dateTo: LocalDate): LiveData<Float?> {
        return repository.getExpectedSum(dateFrom, dateTo).asLiveData()
    }

    fun getMovementsSumGroupByMonth(
        beforeDateInclusive: LocalDate,
        limitData: Int
    ): LiveData<List<GroupInfo>> {
        return repository.getMovementsSumGroupByMonth(beforeDateInclusive, limitData).asLiveData()
    }

    fun getMovementsSumGroupByWeek(
        weekStartOffset: Int,
        beforeDateInclusive: LocalDate,
        limitData: Int
    ): LiveData<List<GroupInfo>> {
        return repository.getMovementsSumGroupByWeek(
            weekStartOffset,
            beforeDateInclusive,
            limitData
        )
            .asLiveData()
    }

    fun getMovementsSumInPeriod(dateFrom: LocalDate, dateTo: LocalDate): LiveData<List<GroupInfo>> {
        return repository.getMovementsSumInPeriod(dateFrom, dateTo).asLiveData()
    }

    fun getGainsAndExpensesInPeriod(dateFrom: LocalDate, dateTo: LocalDate): LiveData<GroupInfo> {
        return repository.getGainsAndExpensesInPeriod(dateFrom, dateTo).asLiveData()
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

    fun setAssetsPeriod(
        from: LocalDate,
        to: LocalDate,
        state: PeriodState,
        datePickerSelection: Pair<Long, Long>?
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                dateFromAssets = from,
                dateToAssets = to,
                stateAssets = state,
                datePickerSelectionAssets = datePickerSelection
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

    fun setIncomingPeriod(
        from: LocalDate,
        to: LocalDate,
        state: PeriodState,
        datePickerSelection: Pair<Long, Long>?
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                dateFromIncoming = from,
                dateToIncoming = to,
                stateIncoming = state,
                datePickerSelectionIncoming = datePickerSelection
            )
        }
    }

    fun setPeriodicPeriod(
        from: LocalDate,
        to: LocalDate,
        state: PeriodState,
        datePickerSelection: Pair<Long, Long>?
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                dateFromPeriodic = from,
                dateToPeriodic = to,
                statePeriodic = state,
                datePickerSelectionPeriodic = datePickerSelection
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

    fun setSelectedCategory(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = name
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

    fun insert(periodicMovement: PeriodicMovement) = viewModelScope.launch {
        repository.insert(periodicMovement)
    }

    suspend fun insert(incomingMovement: IncomingMovement): Long {
        return repository.insert(incomingMovement)
    }

    fun insertDefaultProfile(assets: Float, lastAccess: LocalDate) = viewModelScope.launch {
        repository.insert(Profile(defaultProfileId, assets, lastAccess))
    }


    /*
    Update
     */
    fun update(movement: Movement) = viewModelScope.launch {
        repository.update(movement)
    }

    fun update(category: Category) = viewModelScope.launch {
        repository.update(category)
    }

    fun update(incomingMovement: IncomingMovement) = viewModelScope.launch {
        repository.update(incomingMovement)
    }

    fun update(periodicMovement: PeriodicMovement) = viewModelScope.launch {
        repository.update(periodicMovement)
    }

    fun updateAssets(assetsValue: Float) = viewModelScope.launch {
        repository.updateAssets(defaultProfileId, assetsValue)
    }


    /*
    Delete
     */
    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.delete(category)
    }

    fun deleteMovement(movementId: Int) = viewModelScope.launch {
        repository.deleteMovementFromId(movementId)
    }

    fun deleteIncomingMovement(incomingMovementId: Int) = viewModelScope.launch {
        repository.deleteIncomingMovementFromId(incomingMovementId)
    }

    fun deletePeriodicMovement(periodicMovementId: Int) = viewModelScope.launch {
        repository.deletePeriodicMovement(periodicMovementId)
    }
}