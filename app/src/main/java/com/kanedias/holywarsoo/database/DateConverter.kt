package com.kanedias.holywarsoo.database

import androidx.room.TypeConverter
import java.util.*

/**
 * Utility class to utilize date conversion to/from long for Room databases
 *
 * @author Kanedias
 *
 * Created on 2020-01-12
 */
class DateConverter {

    @TypeConverter
    fun toDate(unixtime: Long) = Date(unixtime)

    @TypeConverter
    fun fromDate(date: Date) = date.time

}