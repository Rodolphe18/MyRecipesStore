package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.model.LikeableRecipe
import com.francotte.myrecipesstore.model.mapToLikeableRecipes
import com.francotte.myrecipesstore.network.api.RecipeApi
import com.francotte.myrecipesstore.user.UserDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val api: RecipeApi,
    private val userDataSource: UserDataSource
) : FavoritesRepository {

    private val cache = mutableMapOf<Long, LikeableRecipe>()

    override fun getFavoritesRecipes(): Flow<Result<List<LikeableRecipe>>> =
        userDataSource.userData.flatMapLatest { userData ->
            flow {
                val ids = userData.favoriteRecipesIds.mapNotNull { it.toLongOrNull() }

                val newIds = ids.filterNot { cache.containsKey(it) }

                newIds.mapNotNull { id ->
                    try {
                        api.getMealDetail(id)
                            .meals
                            .mapToLikeableRecipes(userData)
                            .firstOrNull()
                            ?.also { recipe -> cache[id] = recipe }
                    } catch (_: Exception) {
                        null // Ignore errors for individual items
                    }
                }

                val allRecipes = ids.mapNotNull { cache[it] }
                emit(Result.success(allRecipes))
            }.catch { e ->
                emit(Result.failure(e))
            }
        }
}


interface FavoritesRepository {
    fun getFavoritesRecipes(): Flow<Result<List<LikeableRecipe>>>
}
