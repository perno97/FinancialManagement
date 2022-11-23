package com.perno97.financialmanagement.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category")
    fun getAll(): List<Movement>

    @Query("SELECT * FROM category WHERE categoryId IN (:categoryIds)")
    fun loadAllByIds(categoryIds: IntArray): List<Category>

    @Insert
    fun insertAll(vararg categories: Category)

    @Delete
    fun delete(category: Category)
}