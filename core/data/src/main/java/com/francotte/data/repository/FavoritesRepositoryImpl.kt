package com.francotte.data.repository

import android.net.Uri
import com.francotte.data.interfaces.FavoritesRepository
import com.francotte.data.interfaces.OfflineFirstFavoritesRepository
import com.francotte.data.favorite.FavoriteManager
import com.francotte.data.interfaces.UserDataRepository
import com.francotte.model.CustomIngredient
import com.francotte.model.CustomRecipe
import com.francotte.model.LikeableRecipe
import com.francotte.model.mapToLikeableFullRecipes
import com.francotte.network.model.asDto
import com.francotte.network.model.asExternalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class FavoritesRepositoryImpl @Inject constructor(
    private val offlineFirstFavoritesRepository: OfflineFirstFavoritesRepository,
    private val favoriteManager: FavoriteManager,
    private val userDataRepository: UserDataRepository
) : FavoritesRepository {

    private val customRecipesVersion = MutableStateFlow(0)

    override fun observeFavoritesRecipes(): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataRepository.userData,
            offlineFirstFavoritesRepository.observeFavoritesFullRecipes(),
        ) { userData, favRecipes ->
            try {
                val likeable = favRecipes.mapToLikeableFullRecipes(userData)
                Result.success(likeable)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun refreshFavoritesRecipes() {
        offlineFirstFavoritesRepository.refreshFavoritesFromServer()
    }

    override fun observeUserCustomRecipes(): Flow<Result<List<CustomRecipe>>> =
        customRecipesVersion.flatMapLatest {
            flow {
                try {
                    emit(Result.success(favoriteManager.getUserRecipes().map { it.asExternalModel() }))
                } catch (e: Exception) {
                    emit(Result.failure(e))
                }
            }
        }

    override fun observeUserCustomRecipe(id: String): Flow<Result<CustomRecipe>> =
        flow {
            try {
                val customRecipe = favoriteManager.getUserCustomRecipe(id).asExternalModel()
                emit(Result.success(customRecipe))
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }

    override suspend fun addCustomRecipe(
        title: String,
        ingredients: List<CustomIngredient>,
        instructions: String,
        image: Uri?,
    ): Result<Unit> =
        favoriteManager.createRecipe(title, ingredients.map { it.asDto() }, instructions, image)
            .also { if (it.isSuccess) customRecipesVersion.update { v -> v + 1 } }

    override suspend fun updateCustomRecipe(
        recipeId: String,
        title: String,
        ingredients: List<CustomIngredient>,
        instructions: String,
        image: Uri?,
    ): Result<Unit> =
        favoriteManager.updateRecipe(recipeId, title, ingredients.map { it.asDto() }, instructions, image)
            .also { if (it.isSuccess) customRecipesVersion.update { v -> v + 1 } }
}
