package com.perno97.financialmanagement.database

import android.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Category(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "color") val color: String,
    @ColumnInfo(name = "daily_budget") val budget: Float
) {
}