package com.kanedias.holywarsoo.database

import androidx.room.TypeConverter
import java.util.*


class DateConverter {

    @TypeConverter
    fun toDate(unixtime: Long) = Date(unixtime)

    @TypeConverter
    fun fromDate(date: Date) = date.time

}