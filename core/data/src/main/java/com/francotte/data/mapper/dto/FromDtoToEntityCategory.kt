package com.francotte.data.mapper.dto

import com.francotte.database.model.CategoryEntity
import com.francotte.network.model.NetworkCategory
import java.time.Instant

fun NetworkCategory.asEntity(savedAt:Instant): CategoryEntity =
    CategoryEntity(
        idCategory = idCategory,
        strCategory = strCategory,
        strCategoryThumb = strCategoryThumb,
        strCategoryDescription = strCategoryDescription,
        savedTimestamp = savedAt
    )
