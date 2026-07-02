package com.francotte.data.interfaces

import com.francotte.data.favorite.ToggleFavoriteResult
import com.francotte.model.LikeableRecipe

interface FavoriteHelper {
    suspend fun toggleRecipeFavorite(recipe: LikeableRecipe): ToggleFavoriteResult
}
