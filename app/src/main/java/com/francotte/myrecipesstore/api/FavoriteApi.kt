package com.francotte.myrecipesstore.api

import com.francotte.myrecipesstore.model.RecipeResult
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

//interface FavoriteRecipeApi {
//
//    @GET("users/{userId}/favorites")
//    suspend fun getFavoriteLightRecipes(@Path("userId") userId: Long, @Query("user_token") userToken: String, @Query("page") page: Int): RecipeResult
//
//    @POST("users/{userId}/favorites/{recipeId}")
//    suspend fun addFavorite(
//        @Path("userId") userId: Long,
//        @Path("recipeId") recipeId: Long,
//        @Query("user_token") userToken: String
//    )
//
//    @DELETE("users/{userId}/favorites/{recipeId}")
//    suspend fun removeFavorite(
//        @Path("userId") userId: Long,
//        @Path("recipeId") recipeId: Long,
//        @Query("user_token") userToken: String
//    )
//}