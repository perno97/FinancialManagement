package com.perno97.financialmanagement.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "incumbent_movement")
data class IncumbentMovement(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "incumbent_movement_id") val incumbentMovementId: Int = 0,
    val date: LocalDate,
    val amount: Float,
    val category: String,
    val title: String,
    val notes: String,
    val notify: Boolean
)