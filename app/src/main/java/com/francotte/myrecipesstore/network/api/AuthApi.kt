package com.francotte.myrecipesstore.network.api

import com.francotte.myrecipesstore.network.model.AuthRequest
import com.francotte.myrecipesstore.network.model.AuthResponse
import com.francotte.myrecipesstore.network.model.FacebookAccessTokenRequest
import com.francotte.myrecipesstore.network.model.GoogleIdTokenRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface AuthApi {

    // Manage User by Email/Password

    @POST("users/create")
    suspend fun createUser(
        @Body request: AuthRequest
    ): AuthResponse

    @POST("users/auth")
    suspend fun authUser(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("users/create/google")
    suspend fun createGoogle(@Body request: GoogleIdTokenRequest): Response<AuthResponse>

    @POST("users/auth/google")
    suspend fun authGoogle(@Body request: GoogleIdTokenRequest): Response<AuthResponse>

    @POST("users/create/facebook")
    suspend fun createFacebook(@Body request: FacebookAccessTokenRequest): Response<AuthResponse>

    @POST("users/auth/facebook")
    suspend fun authFacebook(@Body request: FacebookAccessTokenRequest): Response<AuthResponse>

    @DELETE("users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: Long)



}