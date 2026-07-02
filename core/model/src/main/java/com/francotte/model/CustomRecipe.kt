package com.francotte.model

import androidx.compose.runtime.Immutable

@Immutable
data class CustomRecipe(
    val id: String,
    val title: String,
    val ingredients: List<CustomIngredient>,
    val instructions: String,
    val imageUrl: String?,
)

@Immutable
data class CustomIngredient(
    val name: String,
    val quantity: String,
    val measureType: String,
)
