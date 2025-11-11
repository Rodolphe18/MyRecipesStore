package com.francotte.model

sealed class AbstractRecipe {
    abstract val strMeal: String
    abstract val strMealThumb: String?
    abstract val idMeal: String
}