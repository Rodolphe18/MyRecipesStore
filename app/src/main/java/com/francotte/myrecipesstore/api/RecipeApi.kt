package com.francotte.myrecipesstore.api


import com.francotte.myrecipesstore.model.Categories
import com.francotte.myrecipesstore.model.RecipeResult
import retrofit2.http.GET
import retrofit2.http.Query


interface RecipeApi {

    //  Latest Meals
    @GET("latest.php")
    suspend fun getLatestMeals(): RecipeResult

    // Lookup a selection of 10 random meals
    @GET("randomselection.php")
    suspend fun getRandomMealsSelection(): RecipeResult

    //  Search meal by name
    @GET("search.php")
    suspend fun getMealByName(@Query("s") name: String): RecipeResult

    // List all meals by first letter
    @GET("search.php")
    suspend fun getMealByFirstLetter(@Query("f") firstLetter: String): RecipeResult

    // Lookup full meal details by id
    @GET("lookup.php")
    suspend fun getMealDetail(@Query("i") id: Long): RecipeResult

    // Lookup a single random meal
    @GET("random.php")
    suspend fun getRandomMeal(): RecipeResult

    // List all meal categories
    @GET("categories.php")
    suspend fun getAllMealCategories(): Categories

    // List all Categories
    @GET("list.php")
    suspend fun getAllCategories(@Query("c") list: String = "list"): Categories

    // List all Area
    @GET("list.php")
    suspend fun getAllAreas(@Query("a") list: String = "list"): RecipeResult

    // List all Ingredients
    @GET("list.php")
    suspend fun getAllIngredients(@Query("i") list: String = "list"): RecipeResult

    // Filter by multi-ingredient
    @GET("filter.php")
    suspend fun getRecipesListByMultiIngredients(@Query("i") ingredient: String): RecipeResult

    // Filter by main ingredient
    @GET("filter.php")
    suspend fun getRecipesListByMainIngredient(@Query("i") ingredient: String): RecipeResult

    // Filter by Category
    @GET("filter.php")
    suspend fun getRecipesListByCategory(@Query("c") category: String): RecipeResult

    // Filter by Area
    @GET("filter.php")
    suspend fun getRecipesListByArea(@Query("a") area: String): RecipeResult


}