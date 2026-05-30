package com.francotte.data.interfaces

import android.content.Intent
import android.net.Uri
import com.francotte.model.UserData
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val user: StateFlow<UserData?>
    val snackBarMessage: SharedFlow<String>
    val googleSignInIntent: Intent
    suspend fun authenticate(credentials: LoginCredentials): Result<Unit>
    suspend fun register(username: String, email: String, password: String, imageUri: Uri?): Result<Unit>
    suspend fun updateUser(userName: String?, imageUri: Uri?)
    suspend fun logout()
    suspend fun deleteAccount()
    suspend fun requestPasswordReset(email: String): Result<Unit>
}
