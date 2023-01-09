package com.perno97.financialmanagement.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "incoming_movement")
data class IncomingMovement(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "incoming_movement_id") val incomingMovementId: Long = 0,
    val date: LocalDate,
    val amount: Float,
    val category: String,
    val title: String,
    val notes: String,
    val notify: Boolean,
    @ColumnInfo(name = "periodic_movement_id") val periodicMovementId: Long?
)