package com.francotte.auth

interface PasswordResetRepository {
    suspend fun requestPasswordReset(email: String): Result<Unit>
    suspend fun resetPassword(token: String, newPassword: String): Result<Unit>
}
