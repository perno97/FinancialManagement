package com.perno97.financialmanagement.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Movement(
    @PrimaryKey val movementId: Int,
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "amount") val amount: Float,
    @ColumnInfo(name = "category") val category: Int
)
