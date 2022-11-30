package com.perno97.financialmanagement.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long): LocalDate? {
        return value.let { LocalDateTime.ofEpochSecond(value,0,ZoneId.systemDefault()).toLocalDate() }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    }
}