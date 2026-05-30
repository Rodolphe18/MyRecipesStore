package com.francotte.data.interfaces

import com.francotte.model.LikeableRecipe
import kotlinx.coroutines.flow.Flow

interface IngredientsAndAreasRepository {
    fun observeAllIngredients(): Flow<List<String>>

    fun observeAllAreas(): Flow<List<String>>

    suspend fun refreshAllIngredients(force: Boolean): String?

    suspend fun refreshAllAreas(force: Boolean): String?

    suspend fun refreshRecipesByArea(area:String,force:Boolean):String?

    fun observeRecipesByArea(area: String): Flow<Result<List<LikeableRecipe>>>

    suspend fun refreshRecipesByIngredients(ingredients: List<String>,force: Boolean): String?

    fun observeRecipesByIngredients(ingredients: List<String>): Flow<Result<List<LikeableRecipe>>>
}
