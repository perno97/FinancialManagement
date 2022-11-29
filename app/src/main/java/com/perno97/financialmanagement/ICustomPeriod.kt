package com.perno97.financialmanagement

import java.time.LocalDate

interface ICustomPeriod {
    fun setCustomPeriod(from: LocalDate, to: LocalDate)
}
