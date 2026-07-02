package com.francotte.model

import androidx.compose.runtime.Immutable

@Immutable
data class Categories(
    val meals: List<AbstractCategory>,
)

@Immutable
sealed class AbstractCategory {
    abstract val strCategory: String
}

@Immutable
data class LightCategory(
    override val strCategory: String,
) : AbstractCategory()

@Immutable
data class Category(
    val idCategory: String,
    override val strCategory: String,
    val strCategoryThumb: String,
    val strCategoryDescription: String,
) : AbstractCategory()
