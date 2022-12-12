package com.perno97.financialmanagement.database

import java.time.LocalDate

data class GroupInfo(
    val groupDate: LocalDate,
    val positive: Float,
    val negative: Float
)
