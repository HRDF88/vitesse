package com.example.vitesse.data.database

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.util.TimeZone

object RoomTypeConverters {
    /**
     * Converts a timestamp value (Long) to a LocalDateTime object.
     *
     * @param value The timestamp value in milliseconds.
     * @return The equivalent LocalDateTime object.
     */

    @TypeConverter
    @JvmStatic
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), TimeZone.getDefault().toZoneId())
        }
    }

    /**
     * Converts a LocalDateTime object to a timestamp value (Long).
     *
     * @param value The LocalDateTime object.
     * @return The equivalent timestamp value in milliseconds.
     */

    @TypeConverter
    @JvmStatic
    fun toTimestamp(value: LocalDateTime?): Long? {
        return value?.atZone(TimeZone.getDefault().toZoneId())?.toInstant()?.toEpochMilli()
    }
}