package com.francotte.domain


import android.net.Uri
import com.francotte.data.repository.OfflineFirstFavoritesRepository
import com.francotte.datastore.UserDataRepository
import com.francotte.model.CustomIngredient
import com.francotte.model.CustomRecipe
import com.francotte.model.LikeableRecipe
import com.francotte.model.mapToLikeableFullRecipes
import com.francotte.network.model.asDto
import com.francotte.network.model.asExternalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val offlineFirstFavoritesRepository: OfflineFirstFavoritesRepository,
    private val favoriteManager: FavoriteManager,
    private val userDataRepository: UserDataRepository
) : FavoritesRepository {

    override fun observeFavoritesRecipes(): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataRepository.userData,
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
            val customRecipes = favoriteManager.getUserRecipes().map { it.asExternalModel() }
            emit(Result.success(customRecipes))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun observeUserCustomRecipe(id:String): Flow<Result<CustomRecipe>> = flow {
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
        image: Uri?
    ) {
        favoriteManager.createRecipe(title,ingredients.map { it.asDto() },instructions,image)
    }

    override suspend fun updateCustomRecipe(
        recipeId: String,
        title: String,
        ingredients: List<CustomIngredient>,
        instructions: String,
        image: Uri?
    ){
        favoriteManager.updateRecipe(recipeId,title,ingredients.map { it.asDto() },instructions,image)

    }
}


interface FavoritesRepository {
    fun observeFavoritesRecipes(): Flow<Result<List<LikeableRecipe>>>
    fun observeUserCustomRecipes(): Flow<Result<List<CustomRecipe>>>
    fun observeUserCustomRecipe(id:String): Flow<Result<CustomRecipe>>
    suspend fun addCustomRecipe(title: String, ingredients: List<CustomIngredient>, instructions: String, image: Uri?)
    suspend fun updateCustomRecipe(recipeId: String,title: String, ingredients: List<CustomIngredient>, instructions: String, image: Uri?)

}
