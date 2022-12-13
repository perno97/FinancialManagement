package com.perno97.financialmanagement.database

import java.time.LocalDate

data class AmountWithDate(
    val expense: Float,
    val gain: Float,
    val amountDate: LocalDate
)
