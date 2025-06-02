package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.api.RecipeApi
import com.francotte.myrecipesstore.model.Categories
import com.francotte.myrecipesstore.model.RecipeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoriesRepositoryImpl @Inject constructor(
    private val api: RecipeApi
) : CategoriesRepository {

    override fun getAllMealCategories(): Flow<Result<Categories>> {
        return flow {
            val result = api.getAllMealCategories()
            emit(Result.success(result))
        }.catch { e ->
            emit(Result.failure(e))
        }
    }
}

interface CategoriesRepository {
    fun getAllMealCategories(): Flow<Result<Categories>>
}

