package com.perno97.financialmanagement.database

import androidx.room.Embedded
import androidx.room.Relation

data class MovementAndCategory(
    @Embedded val movement: Movement,
    @Relation(
        parentColumn = "category",
        entityColumn = "name"
    )
    val category: Category
)
