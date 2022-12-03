package com.perno97.financialmanagement.utils

enum class PeriodState {
    DAY, WEEK, MONTH, PERIOD;

    companion object {
        val PERIOD_KEY = "periodState"
    }
}