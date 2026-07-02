package com.francotte.data.interfaces

import com.francotte.model.Recipe
import kotlinx.coroutines.flow.Flow

interface OfflineFirstFavoritesRepository {
    fun observeFavoritesFullRecipes(): Flow<List<Recipe>>

    suspend fun refreshFavoritesFromServer()
}
