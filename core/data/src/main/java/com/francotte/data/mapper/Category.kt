package com.francotte.data.mapper

import com.francotte.database.model.CategoryEntity
import com.francotte.network.model.NetworkCategory


fun NetworkCategory.asEntity(): CategoryEntity =
    CategoryEntity(
        idCategory = idCategory,
        strCategory = strCategory,
        strCategoryThumb = strCategoryThumb,
        strCategoryDescription = strCategoryDescription
    )