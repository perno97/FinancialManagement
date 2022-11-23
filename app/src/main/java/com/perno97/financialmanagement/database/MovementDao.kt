package com.perno97.financialmanagement.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MovementDao {
    @Query("SELECT * FROM movement")
    fun getAll(): List<Movement>

    @Query("SELECT * FROM movement WHERE movementId IN (:movementIds)")
    fun loadAllByIds(movementIds: IntArray): List<Movement>

    @Insert
    fun insertAll(vararg movements: Movement)

    @Delete
    fun delete(movement: Movement)
}