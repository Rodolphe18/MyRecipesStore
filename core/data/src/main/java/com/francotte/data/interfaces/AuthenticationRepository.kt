package com.francotte.data.interfaces

import android.content.Intent

interface AuthenticationRepository {
    val googleSignInIntent: Intent
    suspend fun authenticate(credentials: LoginCredentials): Result<Unit>
    suspend fun register(username: String, email: String, password: String, imageUri: android.net.Uri?): Result<Unit>
    suspend fun requestPasswordReset(email: String): Result<Unit>
}
