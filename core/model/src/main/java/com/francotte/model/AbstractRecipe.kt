package com.francotte.model

sealed class AbstractRecipe {
    abstract val strMeal: String
    abstract val strMealThumb: String?
    abstract val idMeal: String
}

data class TestRecipe(
    override val idMeal: String,
    override val strMeal: String,
    override val strMealThumb: String? = null,
) : AbstractRecipe()