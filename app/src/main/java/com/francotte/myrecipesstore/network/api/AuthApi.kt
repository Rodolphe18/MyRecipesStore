package com.francotte.myrecipesstore.network.api

import com.francotte.myrecipesstore.network.model.AuthRequest
import com.francotte.myrecipesstore.network.model.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {

    @POST("users/create")
    suspend fun createUser(
        @Body request: AuthRequest
    ): AuthResponse

    @POST("users/auth")
    suspend fun authenticate(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @DELETE("users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: Long)

}