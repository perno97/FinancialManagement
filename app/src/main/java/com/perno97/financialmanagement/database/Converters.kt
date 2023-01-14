package com.perno97.financialmanagement.database

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long): LocalDate? {
        return value.let {
            Instant.ofEpochSecond(value).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate): Long {
        return date.atTime(12, 0).atZone(ZoneId.systemDefault()).toEpochSecond()
    }
}