package com.perno97.financialmanagement.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PeriodicMovementDao :MovementDao{
    @Query("SELECT * FROM periodic_movement")
    override fun getAll(): List<PeriodicMovement>

    @Query("SELECT * FROM periodic_movement WHERE periodicMovementId IN (:movementIds)")
    override fun loadAllByIds(movementIds: IntArray): List<PeriodicMovement>

    @Insert
    fun insertAll(vararg periodicMovements: PeriodicMovement)

    @Delete
    fun delete(periodicMovement: PeriodicMovement)
}