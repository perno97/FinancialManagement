package com.perno97.financialmanagement.database

import androidx.room.Embedded

data class CategoryAndDailyBudget(
    val budget: Float,
    val category: Category
)