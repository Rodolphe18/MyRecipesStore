package com.francotte.model

data class LikeableRecipe(
    val recipe: AbstractRecipe,
    val favoriteState: FavoriteState
) {
    val isFavorite: Boolean
        get() = favoriteState != FavoriteState.NotFavorite

    val isPending: Boolean
        get() = favoriteState == FavoriteState.PendingAdd || favoriteState == FavoriteState.PendingRemove

    constructor(recipe: AbstractRecipe, userData: UserData) : this(
        recipe = recipe,
        favoriteState = computeFavoriteState(recipe.idMeal, userData),
    )


}

private fun computeFavoriteState(recipeId: String, userData: UserData): FavoriteState {

    if (!userData.isConnected) return FavoriteState.NotFavorite

    val pending = userData.pendingFavorites[recipeId]
    if (pending != null) {
        return if (pending) FavoriteState.PendingAdd else FavoriteState.PendingRemove
    }

    return if (recipeId in userData.favoriteRecipesIds) {
        FavoriteState.FavoriteSynced
    } else {
        FavoriteState.NotFavorite
    }
}

enum class FavoriteState { NotFavorite, PendingAdd, PendingRemove, FavoriteSynced }

fun List<LightRecipe>.mapToLikeableLightRecipes(userData: UserData): List<LikeableRecipe> = mapNotNull { LikeableRecipe(it, userData) }

fun List<Recipe>.mapToLikeableFullRecipes(userData: UserData): List<LikeableRecipe> = mapNotNull { LikeableRecipe(it, userData) }

fun Recipe.mapToLikeableFullRecipe(userData: UserData): LikeableRecipe = LikeableRecipe(recipe = this, userData = userData)
