package com.francotte.data.repository

import android.net.Uri
import com.francotte.model.CustomIngredient
import com.francotte.model.CustomRecipe
import com.francotte.model.LikeableRecipe
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavoritesRecipes(): Flow<Result<List<LikeableRecipe>>>

    suspend fun refreshFavoritesRecipes()

    fun observeUserCustomRecipes(): Flow<Result<List<CustomRecipe>>>

    fun observeUserCustomRecipe(id: String): Flow<Result<CustomRecipe>>

    suspend fun addCustomRecipe(
        title: String,
        ingredients: List<CustomIngredient>,
        instructions: String,
        image: Uri?,
    )

    suspend fun updateCustomRecipe(
        recipeId: String,
        title: String,
        ingredients: List<CustomIngredient>,
        instructions: String,
        image: Uri?,
    )
}
