package com.francotte.auth

import android.content.Context
import android.util.Log
import com.francotte.data.interfaces.UserDataRepository
import com.francotte.data.sync.SyncScheduler
import com.francotte.network.model.AuthResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

enum class AuthOperation { LOGIN, REGISTER, UPDATE }

@Singleton
class AuthSynchronizer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferences: UserDataRepository,
    private val syncScheduler: SyncScheduler,
    private val eventBus: AuthEventBus,
) {
    suspend fun handle(
        response: Response<AuthResponse>,
        operation: AuthOperation = AuthOperation.LOGIN,
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
                    val event = when (operation) {
                        AuthOperation.LOGIN    -> AuthEvent.LoginSuccess(body.user.username ?: "")
                        AuthOperation.REGISTER -> AuthEvent.RegisterSuccess(body.user.username ?: "")
                        AuthOperation.UPDATE   -> AuthEvent.UpdateSuccess
                    }
                    eventBus.emit(event)
                }
                if (operation != AuthOperation.UPDATE) {
                    Log.d("debug_fav_enqueue_for_login", "")
                    syncScheduler.enqueueForLogin(context)
                }
                Result.success(Unit)
            }
            413 -> {
                eventBus.emit(AuthEvent.PayloadTooLarge)
                Result.failure(Exception("413"))
            }
            409 -> {
                eventBus.emit(AuthEvent.UserAlreadyExists)
                Result.failure(Exception("409"))
            }
            else -> {
                eventBus.emit(if (operation == AuthOperation.REGISTER) AuthEvent.RegisterFailed else AuthEvent.LoginFailed)
                Result.failure(Exception("${response.code()}"))
            }
        }
    }
}
