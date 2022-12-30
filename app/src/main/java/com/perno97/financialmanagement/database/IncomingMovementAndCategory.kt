package com.perno97.financialmanagement.database

import androidx.room.Embedded
import androidx.room.Relation

class IncomingMovementAndCategory(
    @Embedded val incomingMovement: IncomingMovement,
    @Relation(
        parentColumn = "category",
        entityColumn = "name"
    )
    val category: Category
)