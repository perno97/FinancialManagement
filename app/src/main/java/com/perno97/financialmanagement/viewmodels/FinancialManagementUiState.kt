package com.perno97.financialmanagement.viewmodels

import com.perno97.financialmanagement.utils.PeriodState
import androidx.core.util.Pair
import com.perno97.financialmanagement.database.Category
import java.time.LocalDate

data class FinancialManagementUiState(
    // MainFragment
    val dateFromMain: LocalDate = LocalDate.of(LocalDate.now().year, LocalDate.now().month, 1),
    val dateToMain: LocalDate = LocalDate.now(),
    val stateMain: PeriodState = PeriodState.MONTH,
    val datePickerSelectionMain: Pair<Long, Long>? = null,

    //CategoryDetailsFragment
    val dateFromCatDetails: LocalDate = LocalDate.of(
        LocalDate.now().year,
        LocalDate.now().month,
        1
    ),
    val dateToCatDetails: LocalDate = LocalDate.now(),
    val stateCatDetails: PeriodState = PeriodState.MONTH,
    val datePickerSelectionCatDetails: Pair<Long, Long>? = null,
    val categoryFilters: List<Category> = listOf(),

    //AddFinancialMovementFragment
    val selectedCategory: String = "",

    //FinancialMovementDetailsFragment
    val selectedCategoryFinMovDet: String = "",

    //AssetsGraphsFragment
    val datePickerSelectionAssets: Pair<Long, Long>? = null,
    val stateAssets: PeriodState = PeriodState.MONTH,
    val dateFromAssets: LocalDate = LocalDate.of(LocalDate.now().year, LocalDate.now().month, 1),
    val dateToAssets: LocalDate = LocalDate.now(),

    //IncomingMovementsFragment TODO remove
    //val dateFromIncoming: LocalDate = LocalDate.of(LocalDate.now().year, LocalDate.now().month, 1),
    //val dateToIncoming: LocalDate = LocalDate.now(),
    //val stateIncoming: PeriodState = PeriodState.MONTH,
    //val datePickerSelectionIncoming: Pair<Long, Long>? = null,

    //PeriodicMovementsFragment TODO remove
    //val datePickerSelectionPeriodic: Pair<Long, Long>? = null,
    //val statePeriodic: PeriodState = PeriodState.MONTH,
    //val dateToPeriodic: LocalDate = LocalDate.now(),
    //val dateFromPeriodic: LocalDate = LocalDate.of(LocalDate.now().year, LocalDate.now().month, 1)
)
