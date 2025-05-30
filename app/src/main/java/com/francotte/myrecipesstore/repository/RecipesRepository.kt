package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.api.RecipeApi
import com.francotte.myrecipesstore.model.RecipeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipesRepositoryImpl @Inject constructor(
    private val api: RecipeApi
): RecipesRepository {

   override fun getLatestMeals(): Flow<RecipeResult?> {
        return flow {
            try {
                emit(api.getLatestMeals())
            } catch (e: Exception) {
                emit(null)
            }
        }
    }

   override fun getRandomMealsSelection(): Flow<RecipeResult?> {
        return flow {
            try {
                emit(api.getRandomMealsSelection())
            } catch (e: Exception) {
                emit(null)
            }
        }
    }

   override fun getMealByName(name: String): Flow<RecipeResult?> {
        return flow {
            try {
                emit(api.getMealByName(name))
            } catch (e: Exception) {
                emit(null)
            }
        }
    }


   override fun getRecipesListByMainIngredient(ingredient: String): Flow<RecipeResult?> {
        return flow {
            try {
                emit(api.getRecipesListByMainIngredient(ingredient))
            } catch (e: Exception) {
                emit(null)
            }
        }
    }

   override fun getRecipesListByCategory(category: String): Flow<RecipeResult?> {
        return flow {
            try {
                emit(api.getRecipesListByCategory(category))
            } catch (e: Exception) {
                emit(null)
            }
        }
    }

   override fun getRecipesListByArea(area: String): Flow<RecipeResult?> {
        return flow {
            try {
                emit(api.getRecipesListByArea(area))
            } catch (e: Exception) {
                emit(null)
            }
        }
    }
}


interface RecipesRepository {
    fun getLatestMeals(): Flow<RecipeResult?>
    fun getRandomMealsSelection(): Flow<RecipeResult?>
    fun getMealByName(name: String): Flow<RecipeResult?>
    fun getRecipesListByMainIngredient(ingredient: String): Flow<RecipeResult?>
    fun getRecipesListByCategory(category: String): Flow<RecipeResult?>
    fun getRecipesListByArea(area: String): Flow<RecipeResult?>
}