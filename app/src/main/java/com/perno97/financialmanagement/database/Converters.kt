package com.perno97.financialmanagement.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long): LocalDate? {
        return value.let { LocalDate.ofEpochDay(value) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate): Long {
        return date.toEpochDay()
    }
}