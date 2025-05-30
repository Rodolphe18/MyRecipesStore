package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.api.RecipeApi
import com.francotte.myrecipesstore.model.RecipeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetailRecipeRepositoryImpl @Inject constructor(
    private val api: RecipeApi
): DetailRecipeRepository {

   override fun getMealDetail(id: Long): Flow<RecipeResult?> {
        return flow {
            try {
                emit(api.getMealDetail(id))
            } catch (e: Exception) {
                emit(null)
            }
        }
    }
}

interface DetailRecipeRepository {
    fun getMealDetail(id: Long): Flow<RecipeResult?>
}

