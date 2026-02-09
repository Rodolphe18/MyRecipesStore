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
class CompositeUserHomeRepository
@Inject
constructor(
    private val offlineFirstHomeRepository: OfflineFirstHomeRepository,
    private val userDataRepository: UserDataRepository,
) : UserHomeRepository {

    override fun observeLatestRecipes(): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataRepository.userData,
            offlineFirstHomeRepository.observeLatestRecipes()
        ) { userData, latest ->
            try {
                Result.success( latest.mapToLikeableFullRecipes(userData))

            } catch (e: Exception) {
                Result.failure(e)
            }
          }

    override suspend fun refreshLatestRecipes(force: Boolean): DataResult<SyncOutcome> {
        return offlineFirstHomeRepository.refreshLatestRecipes(force)
    }

    override fun observeEnglishAreaRecipes(): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataRepository.userData,
            offlineFirstHomeRepository.getRecipesListByArea("British"),
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
            offlineFirstHomeRepository.getRecipesListByArea("American"),
        ) { userData, latestRecipes ->
            try {
                val likeable = latestRecipes.mapToLikeableLightRecipes(userData).take(10)
                Result.success(likeable)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun observeFoodAreaSections(): Flow<Result<Map<String, List<LikeableRecipe>>>> =
        combine(
            userDataRepository.userData,
            combine(
                enumValues<FoodAreaSection>().map { section ->
                    offlineFirstHomeRepository
                        .getRecipesListByArea(section.title)
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
            offlineFirstHomeRepository.getRecipesByCategory(category),
        ) { userData, recipes ->
            try {
                val likeable = recipes.mapToLikeableLightRecipes(userData)
                Result.success(likeable)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun observeFoodAreaSection(sectionName: String): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataRepository.userData,
            offlineFirstHomeRepository.getRecipesListByArea(sectionName),
        ) { userData, recipes ->
            try {
                val likeable = recipes.mapToLikeableLightRecipes(userData)
                Result.success(likeable)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}

interface UserHomeRepository {
    fun observeLatestRecipes(): Flow<Result<List<LikeableRecipe>>>

    suspend fun refreshLatestRecipes(force: Boolean): DataResult<SyncOutcome>

    fun observeEnglishAreaRecipes(): Flow<Result<List<LikeableRecipe>>>

    fun observeAmericanAreaRecipes(): Flow<Result<List<LikeableRecipe>>>

    fun observeFoodAreaSections(): Flow<Result<Map<String, List<LikeableRecipe>>>>

    fun observeFoodAreaSection(sectionName: String): Flow<Result<List<LikeableRecipe>>>

    fun observeRecipesByCategory(category: String): Flow<Result<List<LikeableRecipe>>>
}

private enum class FoodAreaSection(
    val title: String,
) {
    CHINESE("Chinese"),
    FRENCH("French"),
    INDIAN("Indian"),
}
