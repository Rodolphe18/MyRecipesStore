package com.francotte.myrecipesstore.network.api


import com.francotte.myrecipesstore.network.model.NetworkCategories
import com.francotte.myrecipesstore.network.model.NetworkRecipeResult
import retrofit2.http.GET
import retrofit2.http.Query


interface RecipeApi {

    //  Latest Meals
    @GET("latest.php")
    suspend fun getLatestMeals(): NetworkRecipeResult

    // Lookup a selection of 10 random meals
    @GET("randomselection.php")
    suspend fun getRandomMealsSelection(): NetworkRecipeResult

    //  Search meal by name
    @GET("search.php")
    suspend fun getMealByName(@Query("s") name: String): NetworkRecipeResult


    // Lookup full meal details by id
    @GET("lookup.php")
    suspend fun getMealDetail(@Query("i") id: Long): NetworkRecipeResult

    // Lookup a single random meal
    @GET("random.php")
    suspend fun getRandomMeal(): NetworkRecipeResult

    // List all meal categories
    @GET("categories.php")
    suspend fun getAllMealCategories(): NetworkCategories

    // List all Area
    @GET("list.php")
    suspend fun getAllAreas(@Query("a") list: String = "list"): NetworkRecipeResult

    // Filter by multi-ingredient
    @GET("filter.php")
    suspend fun getRecipesListByMultiIngredients(@Query("i") ingredient: String): NetworkRecipeResult

    // Filter by main ingredient
    @GET("filter.php")
    suspend fun getRecipesListByMainIngredient(@Query("i") ingredient: String): NetworkRecipeResult

    // Filter by Category
    @GET("filter.php")
    suspend fun getRecipesListByCategory(@Query("c") category: String): NetworkRecipeResult

    // Filter by Area
    @GET("filter.php")
    suspend fun getRecipesListByArea(@Query("a") area: String): NetworkRecipeResult


}