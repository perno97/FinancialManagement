package com.perno97.financialmanagement.fragments

import com.perno97.financialmanagement.database.Category
import java.time.LocalDate

data class MovementDeletionData(
    val date: LocalDate,
    val title: String,
    val movementId: Int?,
    val incomingMovementId: Int?,
    val periodicMovementId: Int?,
    val notify: Boolean,
    val category: String,
    val amount: Float
)