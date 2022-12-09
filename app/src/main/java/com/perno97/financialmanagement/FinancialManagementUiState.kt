package com.perno97.financialmanagement

import com.perno97.financialmanagement.utils.PeriodState
import androidx.core.util.Pair
import java.time.LocalDate

data class FinancialManagementUiState(
    // MainFragment
    val dateFromMain: LocalDate? = LocalDate.now().minusDays(1),
    val dateToMain: LocalDate? = LocalDate.now(),
    val stateMain: PeriodState? = PeriodState.MONTH,
    val datePickerSelectionMain: Pair<Long, Long>? = null,

    //CategoryDetailsFragment
    val dateFromCatDetails: LocalDate? = LocalDate.now().minusDays(1),
    val dateToCatDetails: LocalDate? = LocalDate.now(),
    val stateCatDetails: PeriodState? = PeriodState.MONTH,
    val datePickerSelectionCatDetails: Pair<Long, Long>? = null
)
