package com.perno97.financialmanagement.database

import android.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Category(
    @PrimaryKey(autoGenerate = true) val categoryId: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "color") val color: Color,
    @ColumnInfo(name = "daily_budget") val budget: Float
)