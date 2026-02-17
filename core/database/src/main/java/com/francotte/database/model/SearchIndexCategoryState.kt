package com.francotte.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "search_index_category_state")
data class SearchIndexCategoryStateEntity(
    @PrimaryKey val strCategory: String,
    val lastIndexedAt: Instant?,
)
