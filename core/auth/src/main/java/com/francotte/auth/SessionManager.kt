package com.francotte.auth

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.francotte.auth.strategy.LoginAuthStrategy
import com.francotte.auth.strategy.LoginCredentials
import com.francotte.common.extension.ApplicationScope
import com.francotte.data.interfaces.UserDataRepository
import com.francotte.database.dao.FullRecipeDao
import com.francotte.network.api.AuthApi
import com.francotte.network.utils.toMultiPartBody
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val loginStrategies: Set<@JvmSuppressWildcards LoginAuthStrategy>,
    private val authSynchronizer: AuthSynchronizer,
    private val preferences: UserDataRepository,
    private val dao: FullRecipeDao,
    private val eventBus: AuthEventBus,
    private val api: AuthApi,
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val coroutineScope: CoroutineScope,
) : SessionRepository {

    private val googleCredentialManager = CredentialManager.create(context)

    override val isAuthenticated: StateFlow<Boolean> = preferences.userData
        .map { it.isAuthenticated }
        .stateIn(coroutineScope, SharingStarted.Eagerly, false)

    override val authEvents: SharedFlow<AuthEvent> = eventBus.events

    override suspend fun login(credentials: LoginCredentials): Result<Unit> =
        loginStrategies.firstNotNullOfOrNull { it.tryAuthenticate(credentials) }
            ?: Result.failure(UnsupportedOperationException("No strategy for ${credentials::class.simpleName}"))

    override suspend fun updateUserInfo(userName: String?, imageUri: Uri?) {
        try {
            val token = preferences.userData.first().token
            authSynchronizer.handle(
                api.updateUserProfile(
                    token = "Bearer $token",
                    username = userName?.toRequestBody("text/plain".toMediaTypeOrNull()),
                    image = imageUri.toMultiPartBody(context),
                ),
                operation = AuthOperation.UPDATE,
            )
        } catch (e: Exception) {
            Log.d("debug_error_while_updating", "Error while updating user")
        }
    }

    override suspend fun logout() {
        try {
            withContext(NonCancellable) {
                val isConnected = preferences.userData.first().isConnected
                eventBus.emit(AuthEvent.Disconnected(wasConnected = isConnected))
                preferences.updateUserInfo(isConnected = false)
                preferences.deleteFavoriteIds()
                googleCredentialManager.clearCredentialState(ClearCredentialStateRequest())
                dao.deleteAllFavoritesRecipes()
            }
        } catch (e: Exception) {
            Log.d("debug_error_while_signing_out", "Error while signing out: $e")
        }
    }

    override suspend fun deleteAccount() {
        val userData = preferences.userData.first()
        if (userData.token.isNullOrBlank()) return
        try {
            withContext(NonCancellable) {
                api.deleteUser(userData.userId)
                preferences.deleteUser()
                eventBus.emit(AuthEvent.AccountDeleted)
                googleCredentialManager.clearCredentialState(ClearCredentialStateRequest())
                dao.deleteAllFavoritesRecipes()
            }
        } catch (e: Exception) {
            Log.d("debug_error_while_signing_out", "Error while signing out: $e")
        }
    }
}
