package com.perno97.financialmanagement.database


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity
open class Movement(
    @PrimaryKey(autoGenerate = true) open val movementId: Int = 0,
    open val date: LocalDate,
    open val amount: Float,
    open val category: String,
    open val title: String,
    open val notes: String,
    open val notify: Boolean
)
