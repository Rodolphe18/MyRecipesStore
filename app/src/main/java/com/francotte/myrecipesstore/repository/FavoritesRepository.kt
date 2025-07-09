package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.database.repository.OfflineFirstFavoritesRepository
import com.francotte.myrecipesstore.datastore.UserDataSource
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.domain.model.mapToLikeableFullRecipes
import com.francotte.myrecipesstore.manager.FavoriteManager
import com.francotte.myrecipesstore.network.model.CustomRecipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val offlineFirstFavoritesRepository: OfflineFirstFavoritesRepository,
    private val favoriteManager: FavoriteManager,
    private val userDataSource: UserDataSource
) : FavoritesRepository {

    override fun observeFavoritesRecipes(): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataSource.userData,
            offlineFirstFavoritesRepository.getFavoritesFullRecipes()
        ) { userData, favRecipes ->
            try {
                val likeable = favRecipes.mapToLikeableFullRecipes(userData)
                Result.success(likeable)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun observeUserCustomRecipes(): Flow<Result<List<CustomRecipe>>> = flow {
       try {
            val customRecipes = favoriteManager.getUserRecipes()
            emit(Result.success(customRecipes))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}


interface FavoritesRepository {
    fun observeFavoritesRecipes(): Flow<Result<List<LikeableRecipe>>>
    fun observeUserCustomRecipes(): Flow<Result<List<CustomRecipe>>>
}
