package com.perno97.financialmanagement.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class Converters {

    @TypeConverter
    fun fromString(value: String): LocalDate? {
        return value.let { LocalDate.parse(value) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate): String? {
        return date.toString()
    }
}