package com.francotte.auth

import com.francotte.network.api.AuthApi
import com.francotte.network.model.EmailRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordResetManager @Inject constructor(
    private val api: AuthApi,
) : PasswordResetRepository {

    override suspend fun requestPasswordReset(email: String): Result<Unit> =
        try {
            val response = api.requestPasswordReset(EmailRequest(email))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Erreur : ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> =
        try {
            val response = api.resetPassword(
                mapOf(
                    "token" to token,
                    "newPassword" to newPassword,
                ),
            )
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Unknown error"))
        } catch (e: Exception) {
            Result.failure(e)
        }
}
