package com.perno97.financialmanagement.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Category(
    @PrimaryKey val name: String,
    val color: String,
    @ColumnInfo(name = "daily_budget") val budget: Float
)