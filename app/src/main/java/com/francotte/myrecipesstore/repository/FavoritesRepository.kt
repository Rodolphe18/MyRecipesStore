package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.database.repository.OfflineFirstFavoritesRepository
import com.francotte.myrecipesstore.datastore.UserDataSource
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.domain.model.mapToLikeableFullRecipes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val offlineFirstFavoritesRepository: OfflineFirstFavoritesRepository,
    private val userDataSource: UserDataSource
) : FavoritesRepository {

    override fun observeFavoritesRecipes(): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataSource.userData,
            offlineFirstFavoritesRepository.getFavoritesFullRecipes()
        ) { userData, latestRecipes ->
            try {
                val likeable = latestRecipes.mapToLikeableFullRecipes(userData)
                Result.success(likeable)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}


interface FavoritesRepository {
    fun observeFavoritesRecipes(): Flow<Result<List<LikeableRecipe>>>
}
