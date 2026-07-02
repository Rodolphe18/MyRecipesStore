package com.francotte.data.interfaces

import com.francotte.model.Recipe
import kotlinx.coroutines.flow.Flow

interface OfflineFirstFullRecipeRepository {
    fun getRecipeDetail(id: Long): Flow<Recipe>
}
