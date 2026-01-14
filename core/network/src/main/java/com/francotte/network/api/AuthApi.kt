package com.francotte.network.api

import com.francotte.network.model.AuthRequest
import com.francotte.network.model.AuthResponse
import com.francotte.network.model.EmailRequest
import com.francotte.network.model.FacebookAccessTokenRequest
import com.francotte.network.model.GoogleIdTokenRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    @DELETE("users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: Long)

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

    @DELETE("users/all")
    suspend fun deleteAllUsers()


}