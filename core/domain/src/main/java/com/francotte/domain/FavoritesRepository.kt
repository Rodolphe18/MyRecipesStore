package com.francotte.domain


import com.francotte.data.repository.OfflineFirstFavoritesRepository
import com.francotte.datastore.UserDataSource
import com.francotte.model.LikeableRecipe
import com.francotte.model.mapToLikeableFullRecipes
import com.francotte.network.model.CustomRecipe
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
