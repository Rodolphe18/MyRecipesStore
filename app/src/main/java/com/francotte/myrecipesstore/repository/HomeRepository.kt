package com.francotte.myrecipesstore.repository

import android.util.Log
import com.francotte.myrecipesstore.database.repository.OfflineFirstHomeRepository
import com.francotte.myrecipesstore.datastore.UserDataSource
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.domain.model.mapToLikeableFullRecipes
import com.francotte.myrecipesstore.domain.model.mapToLikeableLightRecipes
import com.francotte.myrecipesstore.util.FoodAreaSection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HomeRepository @Inject constructor(
    private val homeRepository: OfflineFirstHomeRepository,
    private val userDataSource: UserDataSource
) : LikeableLightRecipesRepository {


    override fun observeLatestRecipes(): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataSource.userData,
            homeRepository.getLatestRecipes()
        ) { userData, latestRecipes ->
            try {
                val likeable = latestRecipes.mapToLikeableFullRecipes(userData)
                Result.success(likeable)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }


    override fun observeEnglishAreaRecipes(): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataSource.userData,
            homeRepository.getRecipesListByArea("British")
        ) { userData, latestRecipes ->
            try {
                val likeable = latestRecipes.mapToLikeableLightRecipes(userData)
                Result.success(likeable)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun observeAmericanAreaRecipes(): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataSource.userData,
            homeRepository.getRecipesListByArea("American")
        ) { userData, latestRecipes ->
            try {
                val likeable = latestRecipes.mapToLikeableLightRecipes(userData).take(10)
                Result.success(likeable)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun observeFoodAreaSections(): Flow<Result<Map<String, List<LikeableRecipe>>>> {
        return combine(
            userDataSource.userData,
            combine(
                enumValues<FoodAreaSection>().map { section ->
                    homeRepository.getRecipesListByArea(section.title)
                        .map { recipeList -> section.title to recipeList }
                }
            ) { it.toMap() }
        ) { userData, recipesMap ->
            try {
                val likeableRecipesMap = recipesMap.mapValues { (_, recipes) ->
                    recipes.mapToLikeableLightRecipes(userData)
                }
                Result.success(likeableRecipesMap)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun observeRecipesByCategory(category: String): Flow<Result<List<LikeableRecipe>>> = combine(
    userDataSource.userData,
    homeRepository.getRecipesByCategory(category)
    ) { userData, recipes ->
        try {
            val likeable = recipes.mapToLikeableLightRecipes(userData)
            Result.success(likeable)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeFoodAreaSection(sectionName:String): Flow<Result<List<LikeableRecipe>>> = combine(
        userDataSource.userData,
        homeRepository.getRecipesListByArea(sectionName)
    ) { userData, recipes ->
        try {
            val likeable = recipes.mapToLikeableLightRecipes(userData)
            Result.success(likeable)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}


interface LikeableLightRecipesRepository {
    fun observeLatestRecipes(): Flow<Result<List<LikeableRecipe>>>
    fun observeEnglishAreaRecipes(): Flow<Result<List<LikeableRecipe>>>
    fun observeAmericanAreaRecipes(): Flow<Result<List<LikeableRecipe>>>
    fun observeFoodAreaSections(): Flow<Result<Map<String, List<LikeableRecipe>>>>
    fun observeFoodAreaSection(sectionName:String): Flow<Result<List<LikeableRecipe>>>
    fun observeRecipesByCategory(category:String):Flow<Result<List<LikeableRecipe>>>
}
