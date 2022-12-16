package com.perno97.financialmanagement.database

import androidx.room.Embedded
import androidx.room.Relation

class CategoryWithMovements(
    @Embedded val category: Category,
    @Relation(
        parentColumn = "name",
        entityColumn = "category"
    )
    val movements: List<Movement>
)