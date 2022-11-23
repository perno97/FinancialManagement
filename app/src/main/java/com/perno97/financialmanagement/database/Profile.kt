package com.perno97.financialmanagement.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Profile(
    @PrimaryKey(autoGenerate = true) val movementId: Int,
    @ColumnInfo(name = "assets") val assets: Float
)
