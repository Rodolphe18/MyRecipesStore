package com.francotte.data.repository

import com.francotte.datastore.UserDataSource
import com.francotte.model.LikeableRecipe
import com.francotte.model.mapToLikeableFullRecipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FullRecipeRepositoryImpl @Inject constructor(
    private val offlineFullRecipeData: OfflineFirstFullRecipeRepository,
    private val userDataSource: UserDataSource
) : FullRecipeRepository {


    override fun observeFullRecipe(id:Long): Flow<Result<LikeableRecipe>> = combine(
    userDataSource.userData,
    offlineFullRecipeData.getRecipeDetail(id)
    ) { userData, fullRecipe ->
        try {
            val likeableRecipe = fullRecipe.mapToLikeableFullRecipe(userData)
            Result.success(likeableRecipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}

interface FullRecipeRepository {
    fun observeFullRecipe(id:Long):Flow<Result<LikeableRecipe>>
}