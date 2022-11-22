package com.perno97.financialmanagement.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MovementDao {
    @Query("SELECT * FROM movement")
    fun getAll(): List<Movement>

    @Query("SELECT * FROM movement WHERE movementId IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Movement>

    @Insert
    fun insertAll(vararg users: Movement)

    @Delete
    fun delete(user: Movement)
}