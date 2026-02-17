package com.francotte.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "area")
data class AreaEntity(@PrimaryKey val strArea: String,val savedTimeStamp: Instant? = null)

@Entity(tableName = "areasFts")
@Fts4
data class AreaFtsEntity(
    @ColumnInfo(name = "strArea") val strArea: String,
    @ColumnInfo(name = "name") val name: String
)

fun AreaEntity.asFtsEntity() = AreaFtsEntity(
    strArea = strArea,
    name = strArea,
)
