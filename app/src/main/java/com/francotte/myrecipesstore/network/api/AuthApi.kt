package com.francotte.myrecipesstore.network.api

import com.francotte.myrecipesstore.network.model.AuthRequest
import com.francotte.myrecipesstore.network.model.AuthResponse
import com.francotte.myrecipesstore.network.model.EmailRequest
import com.francotte.myrecipesstore.network.model.FacebookAccessTokenRequest
import com.francotte.myrecipesstore.network.model.GoogleIdTokenRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface AuthApi {

    @Multipart
    @POST("users/create")
    suspend fun createUser(
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<AuthResponse>

    @Multipart
    @PUT("users/update-profile")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Part("username") username: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<AuthResponse>

    @POST("users/auth")
    suspend fun authUser(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("users/reset-password-request")
    suspend fun requestPasswordReset(
        @Body request: EmailRequest
    ): Response<Unit>

    @POST("users/reset-password/confirm")
    suspend fun resetPassword(@Body data: Map<String, String>): Response<ResponseBody>

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

    @DELETE("users/all")
    suspend fun deleteAllUsers()


}