package com.francotte.data.interfaces

import com.francotte.model.LikeableRecipe
import kotlinx.coroutines.flow.Flow

interface UserFullRecipeRepository {
    fun observeFullRecipe(id: Long): Flow<Result<LikeableRecipe>>
}
