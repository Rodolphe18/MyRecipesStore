package com.francotte.data.repository

import com.francotte.data.interfaces.OfflineFirstFullRecipeRepository
import com.francotte.data.interfaces.UserFullRecipeRepository
import com.francotte.data.interfaces.UserDataRepository
import com.francotte.model.LikeableRecipe
import com.francotte.model.mapToLikeableFullRecipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject


class CompositeUserFullRecipeRepository @Inject constructor(
    private val offlineFullRecipeData: OfflineFirstFullRecipeRepository,
    private val userDataRepository: UserDataRepository
    ) : UserFullRecipeRepository {
        override fun observeFullRecipe(id: Long): Flow<Result<LikeableRecipe>> =
            combine(
                userDataRepository.userData,
                offlineFullRecipeData.getRecipeDetail(id),
            ) { userData, fullRecipe ->
                try {
                    val likeableRecipe = fullRecipe.mapToLikeableFullRecipe(userData)
                    Result.success(likeableRecipe)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
    }


