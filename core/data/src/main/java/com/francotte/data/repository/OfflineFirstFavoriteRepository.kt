package com.francotte.data.repository

import com.francotte.data.mapper.asEntity
import com.francotte.database.dao.FullRecipeDao
import com.francotte.database.model.asExternalModel
import com.francotte.datastore.UserDataRepository
import com.francotte.model.LikeableRecipe
import com.francotte.model.Recipe
import com.francotte.network.api.RecipeApi
import com.francotte.network.model.NetworkRecipe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class OfflineFirstFavoritesRepositoryImpl @Inject constructor(
    private val api: RecipeApi,
    private val dao: FullRecipeDao,
    private val userDataRepository: UserDataRepository
) : OfflineFirstFavoritesRepository {

    private val cache = mutableMapOf<Long, LikeableRecipe>()

    override fun getFavoritesFullRecipes(): Flow<List<Recipe>> =
        userDataRepository.userData.flatMapLatest { userData ->
            val ids = userData.favoriteRecipesIds.mapNotNull { it.toLongOrNull() }

            flow {
                val results = mutableListOf<Recipe>()

                for (id in ids) {
                    val cached = cache[id]
                    if (cached != null) {
                        results.add(cached.recipe as Recipe)
                        continue
                    }

                    val localData = dao.getFullRecipeById(id.toString()).firstOrNull()
                    if (localData != null) {
                        val recipe = localData.asExternalModel()
                        results.add(recipe)
                        cache[id] = LikeableRecipe(recipe, userData)
                    } else {
                        try {
                            val networkData = api.getMealDetail(id)
                                .meals.filterIsInstance<NetworkRecipe>()
                                .firstOrNull()

                            if (networkData != null) {
                                val entity = networkData.asEntity().apply {
                                    isFavorite = true
                                }
                                dao.insertFullRecipe(entity)

                                val recipe = entity.asExternalModel()
                                results.add(recipe)
                                cache[id] = LikeableRecipe(recipe, userData)
                            }
                        } catch (_: Exception) {
                            // skip this ID on failure
                        }
                    }
                }

                emit(results)
            }
        }

}


interface OfflineFirstFavoritesRepository {
    fun getFavoritesFullRecipes(): Flow<List<Recipe>>
}
