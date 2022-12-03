package com.perno97.financialmanagement

import com.perno97.financialmanagement.utils.PeriodState
import androidx.core.util.Pair
import java.time.LocalDate

data class FinancialManagementUiState(
    val dateFrom: LocalDate? = LocalDate.now().minusDays(1),
    val dateTo: LocalDate? = LocalDate.now(),
    val state: PeriodState? = PeriodState.MONTH,
    val datePickerSelection: Pair<Long, Long>? = null
)
