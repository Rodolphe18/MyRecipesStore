package com.francotte.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "latest_list")
data class LatestListEntity(@PrimaryKey val id: Int = 1)
