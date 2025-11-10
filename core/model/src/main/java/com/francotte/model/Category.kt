package com.francotte.model


data class Categories(val meals:List<AbstractCategory>)


sealed class AbstractCategory {
    abstract val strCategory: String
}

data class LightCategory(
    override val strCategory: String
) : AbstractCategory()

data class Category(
    val idCategory: String,
    override val strCategory: String,
    val strCategoryThumb: String,
    val strCategoryDescription: String
) : AbstractCategory()