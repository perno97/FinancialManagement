package com.perno97.financialmanagement.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile")
    fun getAll(): List<Profile>
}