package com.francotte.data.interfaces

import android.net.Uri
import com.francotte.model.UserData
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val user: StateFlow<UserData?>
    suspend fun updateUser(userName: String?, imageUri: Uri?)
}
