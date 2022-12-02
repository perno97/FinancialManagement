package com.perno97.financialmanagement.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.*

@Entity(tableName = "periodic_movement")
data class PeriodicMovement(
    @PrimaryKey(autoGenerate = true) val periodicMovementId: Int,
    val days: Int, //TODO ripetizione ogni giovedì? Ogni 15 del mese?
    override val movementId: Int,
    override val date: LocalDate,
    override val amount: Float,
    override val category: String,
    override val title: String,
    override val notes: String,
    override val notify: Boolean
) : Movement(movementId, date, amount, category, title, notes, notify)