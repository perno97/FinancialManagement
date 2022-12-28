package com.perno97.financialmanagement.utils

import com.perno97.financialmanagement.database.PeriodicMovement

data class CalculatedMovement(
    val periodicMovement: PeriodicMovement,
    val date: String
)
