package com.perno97.financialmanagement.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

//ignoredColumns = ["movementId","date","amount","category"]
@Entity(tableName = "incumbent_movement")
data class IncumbentMovement(
    @PrimaryKey(autoGenerate = true) val incumbentMovementId: Int,
    override val movementId: Int,
    override val date: LocalDate,
    override val amount: Float,
    override val category: String,
    override val title: String,
    override val notes: String,
    override val notify: Boolean
) : Movement(movementId, date, amount, category, title, notes, notify)