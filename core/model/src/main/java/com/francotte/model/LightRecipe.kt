package com.francotte.model

import androidx.compose.runtime.Immutable

@Immutable
data class LightRecipe(
    override val strMeal: String,
    override val strMealThumb: String,
    override val idMeal: String,
) : AbstractRecipe()
