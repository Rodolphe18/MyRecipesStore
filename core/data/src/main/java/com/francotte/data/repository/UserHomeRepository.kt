package com.francotte.data.repository

import com.francotte.model.LikeableRecipe
import kotlinx.coroutines.flow.Flow

interface UserHomeRepository {
    fun observeLatestRecipes(): Flow<List<LikeableRecipe>>

    suspend fun refreshLatestRecipes(force: Boolean): String?

    fun observeEnglishAreaRecipes(): Flow<List<LikeableRecipe>>

    fun observeJapaneseAreaRecipes(): Flow<List<LikeableRecipe>>

    fun observeFoodAreaSections(): Flow<Map<String, List<LikeableRecipe>>>
    suspend fun refreshSpecificFoodAreaSection(area: String, force: Boolean): String?
    suspend fun refreshMultipleFoodAreaSection(force: Boolean): Boolean
    fun observeFoodAreaSection(sectionName: String): Flow<List<LikeableRecipe>>

    suspend fun refreshRecipesByCategory(category: String, force: Boolean): Boolean
    fun observeRecipesByCategory(category: String): Flow<List<LikeableRecipe>>
}
