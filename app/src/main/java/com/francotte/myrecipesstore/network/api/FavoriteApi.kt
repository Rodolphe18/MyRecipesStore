package com.francotte.myrecipesstore.network.api

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoriteApi {

    @GET("users/favorites")
    suspend fun getFavoriteRecipeIds(
        @Header("Authorization") token: String
    ): List<String>

    @GET("users/favorites/{recipeId}/status")
    suspend fun getRecipeFavoriteStatus(
        @Path("recipeId") recipeId: String,
        @Header("Authorization") token: String
    ): Boolean


    @POST("users/favorites/{recipeId}")
    suspend fun addFavorite(
        @Path("recipeId") recipeId: String,
        @Header("Authorization") token: String
    )

    @DELETE("users/favorites/{recipeId}")
    suspend fun removeFavorite(
        @Path("recipeId") recipeId: String,
        @Header("Authorization") token: String
    )
}