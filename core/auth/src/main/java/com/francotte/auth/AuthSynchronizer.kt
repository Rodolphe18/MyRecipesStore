package com.francotte.auth

import com.francotte.data.interfaces.UserDataRepository
import com.francotte.data.sync.FavoritesSyncReason
import com.francotte.data.sync.SyncKind
import com.francotte.data.sync.SyncManager
import com.francotte.network.model.AuthResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

enum class AuthOperation { LOGIN, REGISTER, UPDATE }

@Singleton
class AuthSynchronizer @Inject constructor(
    private val preferences: UserDataRepository,
    private val syncManager: SyncManager,
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
                    // Signing in: push whatever was favorited offline, then reconcile with the
                    // server and prefetch the favorited recipes that are missing locally.
                    syncManager.requestSync(
                        SyncKind.Favorites(FavoritesSyncReason.Login),
                    )
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
