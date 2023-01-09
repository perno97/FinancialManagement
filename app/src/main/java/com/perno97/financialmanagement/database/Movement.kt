package com.perno97.financialmanagement.database


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity
data class Movement(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "movement_id") val movementId: Long = 0,
    val date: LocalDate,
    val amount: Float,
    val category: String,
    val title: String,
    val notes: String,
    @ColumnInfo(name = "periodic_movement_id") val periodicMovementId: Long?
)
