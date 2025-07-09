package com.francotte.myrecipesstore.domain.model


import kotlinx.serialization.Serializable

data class Categories(val meals:List<AbstractCategory>)


sealed class AbstractCategory {
    abstract val strCategory: String
}

@Serializable
data class LightCategory(
    override val strCategory: String
) : AbstractCategory()

@Serializable
data class Category(
    val idCategory: String,
    override val strCategory: String,
    val strCategoryThumb: String,
    val strCategoryDescription: String
) : AbstractCategory()