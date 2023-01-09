package com.perno97.financialmanagement.database


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate


@Entity
data class Profile(
    @PrimaryKey val profileId: Long,
    val assets: Float,
    @ColumnInfo(name = "last_access") val lastAccess: LocalDate
)
