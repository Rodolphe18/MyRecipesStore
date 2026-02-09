package com.francotte.database.converters

import androidx.room.TypeConverter
import java.time.Instant

class InstantConverters {

    @TypeConverter
    fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun longToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)
}
