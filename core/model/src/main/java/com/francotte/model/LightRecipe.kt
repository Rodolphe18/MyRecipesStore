package com.francotte.model

data class LightRecipe(
    override val strMeal: String,
    override val strMealThumb: String,
    override val idMeal: String,
) : AbstractRecipe()