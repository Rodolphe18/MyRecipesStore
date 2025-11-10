package com.francotte.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.francotte.model.Category
import com.francotte.model.LightCategory

@Entity(tableName = "light_category_entity")
data class LightCategoryEntity(@PrimaryKey val strCategory: String)

@Entity(tableName = "full_category_entity")
data class CategoryEntity(
    @PrimaryKey val idCategory: String,
    val strCategory: String,
    val strCategoryThumb: String,
    val strCategoryDescription: String
)

fun LightCategoryEntity.asExternalModel() = LightCategory(strCategory = strCategory)

fun CategoryEntity.asExternalModel() = Category(
    idCategory = idCategory,
    strCategory = strCategory,
    strCategoryThumb = strCategoryThumb,
    strCategoryDescription = strCategoryDescription
)