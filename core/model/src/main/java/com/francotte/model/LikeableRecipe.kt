package com.francotte.model

data class LikeableRecipe(
    val recipe: AbstractRecipe,
    val isFavorite: Boolean
) {
    constructor(recipe: AbstractRecipe, userData: UserData) : this(
        recipe = recipe,
        isFavorite = userData.isConnected && recipe.idMeal in userData.favoriteRecipesIds
    )
}

fun List<LightRecipe>.mapToLikeableLightRecipes(userData: UserData): List<LikeableRecipe> =
    mapNotNull { LikeableRecipe(it, userData) }

fun List<Recipe>.mapToLikeableFullRecipes(userData: UserData): List<LikeableRecipe> =
    mapNotNull { LikeableRecipe(it, userData) }


fun Recipe.mapToLikeableFullRecipe(userData: UserData): LikeableRecipe = LikeableRecipe(recipe = this, userData = userData)