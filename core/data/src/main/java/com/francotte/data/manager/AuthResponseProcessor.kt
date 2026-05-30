package com.francotte.data.manager

import android.content.Context
import android.util.Log
import com.francotte.data.sync.SyncScheduler
import com.francotte.datastore.UserDataRepository
import com.francotte.network.model.AuthResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthResponseProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: UserDataRepository,
    private val syncScheduler: SyncScheduler,
    private val eventBus: AuthEventBus,
) {
    suspend fun handle(
        response: Response<AuthResponse>,
        isRegistering: Boolean = false,
        isUpdating: Boolean = false,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        when (response.code()) {
            200, 202 -> {
                response.body()?.let { body ->
                    preferences.updateUserInfo(
                        isConnected = true,
                        name = body.user.username!!,
                        userId = body.user.userId,
                        userToken = body.token,
                        userEmail = body.user.email ?: "",
                        userImage = body.user.image ?: "",
                    )
                    val message = when {
                        isRegistering -> "Welcome ${body.user.username}! Your account has been created successfully"
                        isUpdating -> "Your account has been updated successfully"
                        else -> "Welcome back ${body.user.username}"
                    }
                    eventBus.emit(message)
                }
                if (!isUpdating) {
                    Log.d("debug_fav_enqueue_for_login", "")
                    syncScheduler.enqueueForLogin(context)
                }
                Result.success(Unit)
            }
            413 -> {
                eventBus.emit("Payload Too Large")
                Result.failure(Exception("413"))
            }
            409 -> {
                eventBus.emit("Your account can't be created : user already exists")
                Result.failure(Exception("409"))
            }
            else -> {
                eventBus.emit(if (isRegistering) "Unknown error. Retry later" else "Email/Password combination failed !")
                Result.failure(Exception("${response.code()}"))
            }
        }
    }
}
