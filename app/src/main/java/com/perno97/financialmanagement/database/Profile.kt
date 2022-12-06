package com.perno97.financialmanagement.database


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Profile(
    @PrimaryKey val profileId: Int,
    val assets: Float
)
