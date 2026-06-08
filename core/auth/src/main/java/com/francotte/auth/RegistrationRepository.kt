package com.francotte.auth

import android.net.Uri

interface RegistrationRepository {
    suspend fun register(username: String, email: String, password: String, imageUri: Uri?): Result<Unit>
    suspend fun requestPasswordReset(email: String): Result<Unit>
}
