package com.perno97.financialmanagement.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "periodic_movement")
data class PeriodicMovement(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "periodic_movement_id") val periodicMovementId: Long = 0,
    val days: Int,
    val months: Int,
    val monday: Boolean,
    val tuesday: Boolean,
    val wednesday: Boolean,
    val thursday: Boolean,
    val friday: Boolean,
    val saturday: Boolean,
    val sunday: Boolean,
    val date: LocalDate,
    val amount: Float,
    val category: String,
    val title: String,
    val notes: String,
    val notify: Boolean
)