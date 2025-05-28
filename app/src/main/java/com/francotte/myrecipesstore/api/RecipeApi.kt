package com.francotte.myrecipesstore.api


import com.francotte.myrecipesstore.model.MealResult
import retrofit2.http.GET
import retrofit2.http.Query


interface RecipeApi {

    //  Search meal by name
    @GET("search.php")
    suspend fun getMealByName(@Query("s") name: String): MealResult

    // List all meals by first letter
    @GET("search.php")
    suspend fun getMealByFirstLetter(@Query("f") firstLetter: String): MealResult

    // Lookup full meal details by id
    @GET("lookup.php")
    suspend fun getMealDetail(@Query("i") id: Long): MealResult

    // Lookup a single random meal
    @GET("random.php")
    suspend fun getRandomMeal(): MealResult

    // List all meal categories
    @GET("categories.php")
    suspend fun getAllMealCategories(): MealResult

    // List all Categories
    @GET("list.php")
    suspend fun getAllCategories(@Query("c") list: String = "list"): MealResult

    // List all Area
    @GET("list.php")
    suspend fun getAllAreas(@Query("a") list: String = "list"): MealResult

    // List all Ingredients
    @GET("list.php")
    suspend fun getAllIngredients(@Query("i") list: String = "list"): MealResult

    // Filter by main ingredient
    @GET("filter.php")
    suspend fun getRecipesListByMainIngredient(@Query("i") ingredient: String): MealResult

    // Filter by Category
    @GET("filter.php")
    suspend fun getRecipesListByCategory(@Query("c") category: String): MealResult

    // Filter by Area
    @GET("filter.php")
    suspend fun getRecipesListByArea(@Query("a") area: String): MealResult


}