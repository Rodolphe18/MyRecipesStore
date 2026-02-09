package com.francotte.data.mapper.entity

import com.francotte.database.model.CategoryEntity
import com.francotte.database.model.LightCategoryEntity
import com.francotte.model.Category
import com.francotte.model.LightCategory

fun LightCategoryEntity.asExternalModel() = LightCategory(strCategory = strCategory)

fun CategoryEntity.asExternalModel() =
    Category(
        idCategory = idCategory,
        strCategory = strCategory,
        strCategoryThumb = strCategoryThumb,
        strCategoryDescription = strCategoryDescription,
    )
