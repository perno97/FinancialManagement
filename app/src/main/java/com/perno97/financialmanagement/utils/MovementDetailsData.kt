package com.perno97.financialmanagement.utils

import java.time.DayOfWeek
import java.time.LocalDate

data class MovementDetailsData(
    val movementId: Long?,
    val date: LocalDate,
    val amount: Float,
    val category: String,
    val color: String,
    val title: String,
    val notes: String,
    val periodicMovementId: Long?,
    val weekDays: List<DayOfWeek>?,
    val days: Int,
    val months: Int,
    val notify: Boolean,
    val incomingMovementId: Long?
)