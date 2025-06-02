package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.api.RecipeApi
import com.francotte.myrecipesstore.model.RecipeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipesRepositoryImpl @Inject constructor(
    private val api: RecipeApi
) : RecipesRepository {

   override fun getLatestMeals(): Flow<Result<RecipeResult>> = flow {
        val result = api.getLatestMeals()
        emit(Result.success(result))
    }.catch { e ->
        emit(Result.failure(e))
    }

   override fun getRandomMealsSelection(): Flow<Result<RecipeResult>> = flow {
        val result = api.getRandomMealsSelection()
        emit(Result.success(result))
    }.catch { e ->
        emit(Result.failure(e))
    }

    override fun getRecipesListByCategory(category: String): Flow<Result<RecipeResult>> {
        return flow {
            val result = api.getRecipesListByCategory(category)
            emit(Result.success(result))
        }.catch { e ->
            emit(Result.failure(e))
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
    fun getLatestMeals(): Flow<Result<RecipeResult>>
    fun getRandomMealsSelection(): Flow<Result<RecipeResult>>
    fun getMealByName(name: String): Flow<RecipeResult?>
    fun getRecipesListByMainIngredient(ingredient: String): Flow<RecipeResult?>
    fun getRecipesListByCategory(category: String): Flow<Result<RecipeResult>>
    fun getRecipesListByArea(area: String): Flow<RecipeResult?>
}
