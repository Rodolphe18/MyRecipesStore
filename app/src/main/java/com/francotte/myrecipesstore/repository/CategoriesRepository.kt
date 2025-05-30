package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.api.RecipeApi
import com.francotte.myrecipesstore.model.Categories
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoriesRepositoryImpl @Inject constructor(
    private val api: RecipeApi
) : CategoriesRepository {

    override fun getAllMealCategories(): Flow<Categories?> {
        return flow {
            try {
                emit(api.getAllMealCategories())
            } catch (e: Exception) {
                emit(null)
            }
        }
    }
}

interface CategoriesRepository {
    fun getAllMealCategories(): Flow<Categories?>
}

