package com.perno97.financialmanagement.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.*

@Entity
open class Movement(
    @PrimaryKey(autoGenerate = true) open val movementId: Int = 0,
    @ColumnInfo(name = "date") open val date: LocalDate,
    @ColumnInfo(name = "amount") open val amount: Float,
    @ColumnInfo(name = "category") open val category: Int
)
