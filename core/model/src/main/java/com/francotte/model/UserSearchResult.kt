package com.francotte.model

data class UserSearchResult(
    val categories: List<String> = emptyList(),
    val areas: List<String> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val likeableRecipes: List<LikeableRecipe> = emptyList(),
)

