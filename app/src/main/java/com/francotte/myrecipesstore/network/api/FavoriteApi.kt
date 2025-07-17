package com.francotte.myrecipesstore.network.api

import com.francotte.myrecipesstore.network.model.CustomRecipe
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface FavoriteApi {

    @GET("users/favorites")
    suspend fun getFavoriteRecipeIds(
        @Header("Authorization") token: String
    ): List<String>


    @GET("users/recipes")
    suspend fun getUserRecipes(
        @Header("Authorization") token: String
    ): List<CustomRecipe>

    @GET("users/recipes/{recipeId}")
    suspend fun getUserRecipe(
        @Header("Authorization") token: String,
        @Path("recipeId") recipeId: String
    ): CustomRecipe

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

    @Multipart
    @POST("users/recipes")
    suspend fun addRecipe(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part?,
        @Part("title") title: RequestBody,
        @Part("instructions") instructions: RequestBody,
        @Part("ingredients") ingredients: RequestBody
    )

    @Multipart
    @PUT("users/recipes/{recipeId}")
    suspend fun updateRecipe(
        @Header("Authorization") token: String,
        @Path("recipeId") recipeId: String,
        @Part image: MultipartBody.Part?,
        @Part("title") title: RequestBody,
        @Part("instructions") instructions: RequestBody,
        @Part("ingredients") ingredients: RequestBody
    ): Response<Unit>

}