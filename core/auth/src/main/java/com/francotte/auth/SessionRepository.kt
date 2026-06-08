package com.francotte.auth

import android.net.Uri
import com.francotte.auth.strategy.LoginCredentials
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {
    val isAuthenticated: StateFlow<Boolean>
    val authEvents: SharedFlow<AuthEvent>
    suspend fun login(credentials: LoginCredentials): Result<Unit>
    suspend fun logout()
    suspend fun deleteAccount()
    suspend fun updateUserInfo(userName: String?, imageUri: Uri?)
}
