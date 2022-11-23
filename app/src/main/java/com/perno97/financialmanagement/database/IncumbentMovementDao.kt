package com.perno97.financialmanagement.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface IncumbentMovementDao :MovementDao{
    @Query("SELECT * FROM incumbent_movement")
    override fun getAll(): List<IncumbentMovement>

    @Query("SELECT * FROM incumbent_movement WHERE movementId IN (:movementIds)")
    override fun loadAllByIds(movementIds: IntArray): List<IncumbentMovement>

    @Insert
    fun insertAll(vararg incumbentMovements: IncumbentMovement)

    @Delete
    fun delete(incumbentMovement: IncumbentMovement)
}