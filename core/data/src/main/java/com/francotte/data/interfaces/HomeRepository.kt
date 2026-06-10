package com.francotte.data.interfaces

import com.francotte.common.utils.DataResult
import com.francotte.model.LightRecipe
import com.francotte.model.Recipe
import kotlinx.coroutines.flow.Flow

interface HomeRepository {

    fun observeLatestRecipes(): Flow<List<Recipe>>

    suspend fun refreshLatestRecipes(force: Boolean = false): String?

    fun observeRecipesListByArea(area: String): Flow<List<LightRecipe>>

    suspend fun refreshRecipesListByArea(area: String, force: Boolean): String?

    fun observeRecipesByCategory(category: String): Flow<List<LightRecipe>?>

    suspend fun refreshRecipesByCategory(category: String, force: Boolean): Boolean

    /** One-shot network fetch, no DB write. */
    suspend fun getRecipesByCategory(category: String): DataResult<List<LightRecipe>>

    /** One-shot network fetch, no DB write. */
    suspend fun getRecipesByArea(area: String): DataResult<List<LightRecipe>>

    /** One-shot network fetch, no DB write. */
    suspend fun getRecipesByIngredients(ingredients: List<String>): DataResult<List<LightRecipe>>

    fun observeRecipesByIngredients(ingredients: List<String>): Flow<List<LightRecipe>>

    suspend fun refreshRecipesByIngredients(ingredients: List<String>, force: Boolean): String?
}

