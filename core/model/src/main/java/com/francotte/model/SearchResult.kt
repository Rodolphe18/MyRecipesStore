package com.francotte.model

import androidx.compose.runtime.Immutable

@Immutable
data class SearchResult(
    val categories: List<String> = emptyList(),
    val areas: List<String> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val lightRecipes: List<LightRecipe> = emptyList(),
)
