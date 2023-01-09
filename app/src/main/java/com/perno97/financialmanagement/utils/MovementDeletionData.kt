package com.perno97.financialmanagement.utils

import java.time.LocalDate

data class MovementDeletionData(
    val date: LocalDate,
    val title: String,
    val movementId: Long?,
    val incomingMovementId: Long?,
    val periodicMovementId: Long?,
    val notify: Boolean,
    val category: String,
    val amount: Float
)