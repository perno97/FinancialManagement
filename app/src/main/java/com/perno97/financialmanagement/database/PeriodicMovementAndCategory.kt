package com.perno97.financialmanagement.database

import androidx.room.Embedded
import androidx.room.Relation

class PeriodicMovementAndCategory(
    @Embedded val periodicMovement: PeriodicMovement,
    @Relation(
        parentColumn = "category",
        entityColumn = "name"
    )
    val category: Category
)