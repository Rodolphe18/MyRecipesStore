package com.francotte.data.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.francotte.common.extension.ApplicationScope
import com.francotte.data.interfaces.AuthRepository
import com.francotte.data.interfaces.LoginAuthStrategy
import com.francotte.data.interfaces.LoginCredentials
import com.francotte.database.dao.FullRecipeDao
import com.francotte.datastore.UserDataRepository
import com.francotte.network.api.AuthApi
import com.francotte.network.model.EmailRequest
import com.francotte.data.sync.SyncScheduler
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: AuthApi,
    private val preferences: UserDataRepository,
    private val dao: FullRecipeDao,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val syncScheduler: SyncScheduler,
    private val strategies: Set<@JvmSuppressWildcards LoginAuthStrategy>,
    private val processor: AuthResponseProcessor,
    private val eventBus: AuthEventBus,
) : AuthRepository {

    private val googleSignInClient =
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("431140586774-o1p6cnlk42macn41t4ld0t0bh2kr21fi.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build(),
        )

    override val googleSignInIntent: Intent get() = googleSignInClient.signInIntent
    override val snackBarMessage: SharedFlow<String> = eventBus.snackBarMessage

    private val userDataFlow = preferences.userData

    val userCredentials = userDataFlow.map { userData ->
        UserCredentials(userData.userId, userData.token ?: return@map null)
    }
    val credentials = userCredentials.stateIn(coroutineScope, SharingStarted.Eagerly, null)

    private val authenticatedFlow =
        userDataFlow.map { it.isConnected && it.token?.isNotBlank() == true }
    val isAuthenticated = authenticatedFlow.stateIn(coroutineScope, SharingStarted.Eagerly, false)

    override val user = userDataFlow.stateIn(coroutineScope, SharingStarted.Eagerly, null)

    val authToken = credentials.value?.token

    override suspend fun authenticate(credentials: LoginCredentials): Result<Unit> =
        strategies.firstNotNullOfOrNull { it.tryAuthenticate(credentials) }
            ?: Result.failure(UnsupportedOperationException("No strategy for ${credentials::class.simpleName}"))

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        imageUri: Uri?,
    ): Result<Unit> =
        try {
            processor.handle(
                api.createUser(
                    username = username.toRequestBody("text/plain".toMediaTypeOrNull()),
                    email = email.toRequestBody("text/plain".toMediaTypeOrNull()),
                    password = password.toRequestBody("text/plain".toMediaTypeOrNull()),
                    image = imageUri.toMultiPartBody(context),
                ),
                isRegistering = true,
            )
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun updateUser(userName: String?, imageUri: Uri?) {
        try {
            processor.handle(
                api.updateUserProfile(
                    token = "Bearer ${credentials.value?.token}",
                    username = userName?.toRequestBody("text/plain".toMediaTypeOrNull()),
                    image = imageUri.toMultiPartBody(context),
                ),
                isUpdating = true,
            )
        } catch (e: Exception) {
            Log.d("debug_error_while_updating", "Error while updating user")
        }
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> =
        try {
            val response = api.requestPasswordReset(EmailRequest(email))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Erreur : ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun deleteAccount() {
        val creds = credentials.value ?: return
        try {
            withContext(NonCancellable) {
                api.deleteUser(creds.id)
                preferences.deleteUser()
                eventBus.emit("Your account has been deleted successfully")
                googleSignInClient.signOut().await()
                dao.deleteAllFavoritesRecipes()
            }
        } catch (e: Exception) {
            Log.d("debug_error_while_signing_out", "Error while signing out")
        }
    }

    suspend fun deleteAllUsers() {
        api.deleteAllUsers()
    }

    override suspend fun logout() {
        try {
            withContext(NonCancellable) {
                eventBus.emit(if (isAuthenticated.value) "You have been disconnected" else "You are not connected")
                preferences.updateUserInfo(false)
                preferences.deleteFavoriteIds()
                googleSignInClient.signOut().await()
                dao.deleteAllFavoritesRecipes()
            }
        } catch (e: Exception) {
            Log.d("debug_error_while_signing_out", "Error while signing out")
        }
    }
}

data class UserCredentials(
    val id: Long,
    val token: String,
)
