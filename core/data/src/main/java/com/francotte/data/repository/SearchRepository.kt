package com.francotte.data.repository

import com.francotte.datastore.UserDataRepository
import com.francotte.model.LikeableRecipe
import com.francotte.model.mapToLikeableLightRecipes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val homeRepository: OfflineFirstHomeRepository,
    private val offlineSearchRepository: OfflineSearchRepository,
    private val userDataRepository: UserDataRepository
): SearchRepository {

    override fun observeAllIngredients(): Flow<List<String>> {
       return offlineSearchRepository.getAllIngredients().map { list -> list.map { ingredient -> ingredient.strIngredient } }
    }

    override fun observeAllAreas(): Flow<List<String>> {
        return offlineSearchRepository.getAllAreas().map { list -> list.map { area -> area.strArea } }
    }

    override fun observeRecipesByArea(area:String): Flow<Result<List<LikeableRecipe>>> = combine(
        userDataRepository.userData,
        homeRepository.getRecipesListByArea(area)
    ) { userData, recipes ->
        try {
            val likeable = recipes.mapToLikeableLightRecipes(userData)
            Result.success(likeable)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeRecipesByIngredients(ingredients: List<String>): Flow<Result<List<LikeableRecipe>>> = combine(
        userDataRepository.userData,
        homeRepository.getRecipesByIngredients(ingredients)
        ) { userData, recipes ->
            try {
                val likeable = recipes.mapToLikeableLightRecipes(userData)
                Result.success(likeable)
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
}

interface SearchRepository {
    fun observeAllIngredients(): Flow<List<String>>
    fun observeAllAreas(): Flow<List<String>>
    fun observeRecipesByArea(area:String): Flow<Result<List<LikeableRecipe>>>
    fun observeRecipesByIngredients(ingredients: List<String>): Flow<Result<List<LikeableRecipe>>>
}