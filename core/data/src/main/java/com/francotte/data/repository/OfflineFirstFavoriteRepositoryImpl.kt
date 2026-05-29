package com.francotte.data.repository

import com.francotte.data.mapper.dto.asEntity
import com.francotte.data.mapper.entity.asExternalModel
import com.francotte.database.dao.FullRecipeDao
import com.francotte.datastore.UserDataRepository
import com.francotte.model.Recipe
import com.francotte.network.api.FavoriteApi
import com.francotte.network.api.RecipeApi
import com.francotte.network.model.NetworkRecipe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstFavoritesRepositoryImpl @Inject constructor(
    private val fullRecipeDao: FullRecipeDao,
    private val favoriteApi: FavoriteApi,
    private val recipeApi: RecipeApi,
    private val userDataRepository: UserDataRepository,
) : OfflineFirstFavoritesRepository {

    override fun observeFavoritesFullRecipes(): Flow<List<Recipe>> =
        userDataRepository.userData
            .map { it.favoriteRecipesIds.mapNotNull(String::toLongOrNull).map(Long::toString) }
            .distinctUntilChanged()
            .flatMapLatest { ids ->
                if (ids.isEmpty()) return@flatMapLatest flowOf(emptyList())
                fullRecipeDao.observeFullRecipesByIds(ids)
                    .map { entities ->
                        val map = entities.associateBy { it.idMeal }
                        ids.mapNotNull(map::get).map { it.asExternalModel() }
                    }
            }

    override suspend fun refreshFavoritesFromServer() {
        val user = userDataRepository.userData.first()
        val token = user.token ?: return
        if (!user.isConnected || token.isBlank()) return

        // 1) Pull serveur -> IDs
        val serverIds = favoriteApi.getFavoriteRecipeIds("Bearer $token")
        userDataRepository.setFavoritesIds(serverIds.toSet())

        // 2) Prefetch détails -> Room
        val ids = serverIds.distinct()
        if (ids.isEmpty()) return

        val existing = fullRecipeDao.getExistingIds(ids).toSet()
        val missing = ids.filterNot(existing::contains)
        if (missing.isEmpty()) return

        // Séquentiel (simple). On peut paralléliser ensuite.
        for (idStr in missing) {
            val idLong = idStr.toLongOrNull() ?: continue
            val networkRecipe =
                recipeApi.getMealDetail(idLong)
                    .meals
                    .filterIsInstance<NetworkRecipe>()
                    .firstOrNull() ?: continue

            fullRecipeDao.insertFullRecipe(networkRecipe.asEntity())
        }
    }
}
