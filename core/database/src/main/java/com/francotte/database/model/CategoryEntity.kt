package com.francotte.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.francotte.model.Category
import com.francotte.model.LightCategory
import java.time.Instant

@Entity(tableName = "light_category_entity")
data class LightCategoryEntity(@PrimaryKey val strCategory: String)

@Entity(tableName = "full_category_entity")
data class CategoryEntity(
    @PrimaryKey val idCategory: String,
    val strCategory: String,
    val strCategoryThumb: String,
    val strCategoryDescription: String,
    val savedTimestamp: Instant?=null
)

@Entity(tableName = "categoriesFts")
@Fts4
data class CategoryFtsEntity(
    @ColumnInfo(name = "strCategory") val strCategory: String,
    @ColumnInfo(name = "name") val name: String
)

fun CategoryEntity.asFtsEntity() = CategoryFtsEntity(
    strCategory = strCategory,
    name = strCategory,
)
