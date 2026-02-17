package com.francotte.data.repository

import com.francotte.datastore.UserDataRepository
import com.francotte.model.LikeableRecipe
import com.francotte.model.mapToLikeableFullRecipes
import com.francotte.model.mapToLikeableLightRecipes
import com.francotte.common.utils.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompositeUserHomeRepository @Inject constructor(
    private val offlineFirstHomeRepository: OfflineFirstHomeRepository,
    private val userDataRepository: UserDataRepository,
) : UserHomeRepository {

    override fun observeLatestRecipes(): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataRepository.userData,
            offlineFirstHomeRepository.observeLatestRecipes()
        ) { userData, latest ->
            try {
                Result.success(latest.mapToLikeableFullRecipes(userData))

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun refreshLatestRecipes(force: Boolean): String? {
        return offlineFirstHomeRepository.refreshLatestRecipes(force)
    }

    override fun observeEnglishAreaRecipes(): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataRepository.userData,
            offlineFirstHomeRepository.observeRecipesListByArea("British"),
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
            userDataRepository.userData,
            offlineFirstHomeRepository.observeRecipesListByArea("American"),
        ) { userData, latestRecipes ->
            try {
                val likeable = latestRecipes.mapToLikeableLightRecipes(userData).take(10)
                Result.success(likeable)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun refreshAllFoodAreaSection(force: Boolean): Boolean {
        enumValues<FoodAreaSection>().map { section ->
            offlineFirstHomeRepository
                .refreshRecipesListByArea(section.title, force)
        }
        offlineFirstHomeRepository.refreshRecipesListByArea("British", force)
        offlineFirstHomeRepository.refreshRecipesListByArea("American", force)
        return true
    }

    override suspend fun refreshFoodAreaSection(area: String, force: Boolean): String? {
        return offlineFirstHomeRepository.refreshRecipesListByArea(area, force)
    }


    override fun observeFoodAreaSections(): Flow<Result<Map<String, List<LikeableRecipe>>>> =
        combine(
            userDataRepository.userData,
            combine(
                enumValues<FoodAreaSection>().map { section ->
                    offlineFirstHomeRepository
                        .observeRecipesListByArea(section.title)
                        .map { recipeList -> section.title to recipeList }
                },
            ) { it.toMap() },
        ) { userData, recipesMap ->
            try {
                val likeableRecipesMap =
                    recipesMap.mapValues { (_, recipes) ->
                        recipes.mapToLikeableLightRecipes(userData)
                    }
                Result.success(likeableRecipesMap)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }


    override fun observeRecipesByCategory(category: String): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataRepository.userData,
            offlineFirstHomeRepository.observeRecipesByCategory(category),
        ) { userData, recipes ->
            try {
                val recipesNotNull = recipes ?: emptyList()
                val likeable = recipesNotNull.mapToLikeableLightRecipes(userData)
                Result.success(likeable)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }


    override fun observeFoodAreaSection(sectionName: String): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataRepository.userData,
            offlineFirstHomeRepository.observeRecipesListByArea(sectionName),
        ) { userData, recipes ->
            try {
                val likeable = recipes.mapToLikeableLightRecipes(userData)
                Result.success(likeable)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun refreshRecipesByCategory(
        category: String,
        force: Boolean
    ): Boolean {
        return offlineFirstHomeRepository.refreshRecipesByCategory(category, force)
    }
}

interface UserHomeRepository {
    fun observeLatestRecipes(): Flow<Result<List<LikeableRecipe>>>

    suspend fun refreshLatestRecipes(force: Boolean): String?

    fun observeEnglishAreaRecipes(): Flow<Result<List<LikeableRecipe>>>

    fun observeAmericanAreaRecipes(): Flow<Result<List<LikeableRecipe>>>

    fun observeFoodAreaSections(): Flow<Result<Map<String, List<LikeableRecipe>>>>
    suspend fun refreshFoodAreaSection(area: String, force: Boolean): String?
    suspend fun refreshAllFoodAreaSection(force: Boolean): Boolean
    fun observeFoodAreaSection(sectionName: String): Flow<Result<List<LikeableRecipe>>>

    suspend fun refreshRecipesByCategory(category: String, force: Boolean): Boolean
    fun observeRecipesByCategory(category: String): Flow<Result<List<LikeableRecipe>>>
}

private enum class FoodAreaSection(
    val title: String,
) {
    CHINESE("Chinese"),
    FRENCH("French"),
    INDIAN("Indian"),
}
