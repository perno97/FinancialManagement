package com.perno97.financialmanagement.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

//ignoredColumns = ["movementId","date","amount","category"]
@Entity(tableName = "incumbent_movement")
data class IncumbentMovement(
    @PrimaryKey(autoGenerate = true) val incumbentMovementId: Int,
    override val movementId: Int,
    override val date: Date,
    override val amount: Float,
    override val category: Int
) :Movement(movementId, date, amount, category)