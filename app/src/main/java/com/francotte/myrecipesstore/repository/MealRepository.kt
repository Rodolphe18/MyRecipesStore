package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.api.RecipeApi
import com.francotte.myrecipesstore.model.MealResult
import com.francotte.myrecipesstore.util.Resource
import com.francotte.myrecipesstore.util.safeApiCall
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class MealRepository @Inject constructor(
    private val api: RecipeApi
) {

    fun getMealByName(name: String): Flow<Resource<MealResult>> =
        safeApiCall { api.getMealByName(name) }

    fun getMealByFirstLetter(firstLetter: String): Flow<Resource<MealResult>> =
        safeApiCall { api.getMealByFirstLetter(firstLetter) }

    fun getMealDetail(id: Long): Flow<Resource<MealResult>> =
        safeApiCall { api.getMealDetail(id) }

    fun getRandomMeal(): Flow<Resource<MealResult>> =
        safeApiCall { api.getRandomMeal() }

    fun getAllMealCategories(): Flow<Resource<MealResult>> =
        safeApiCall { api.getAllMealCategories() }

    fun getAllCategories(): Flow<Resource<MealResult>> =
        safeApiCall { api.getAllCategories() }

    fun getAllAreas(): Flow<Resource<MealResult>> =
        safeApiCall { api.getAllAreas() }

    fun getAllIngredients(): Flow<Resource<MealResult>> =
        safeApiCall { api.getAllIngredients() }

    fun getRecipesListByMainIngredient(ingredient: String): Flow<Resource<MealResult>> =
        safeApiCall { api.getRecipesListByMainIngredient(ingredient) }

    fun getRecipesListByCategory(category: String): Flow<Resource<MealResult>> =
        safeApiCall { api.getRecipesListByCategory(category) }

    fun getRecipesListByArea(area: String): Flow<Resource<MealResult>> =
        safeApiCall { api.getRecipesListByArea(area) }
}
