package com.francotte.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.francotte.common.extension.ApplicationScope
import com.francotte.database.dao.FullRecipeDao
import com.francotte.datastore.UserDataRepository
import com.francotte.network.api.AuthApi
import com.francotte.network.model.AuthRequest
import com.francotte.network.model.AuthResponse
import com.francotte.network.model.EmailRequest
import com.francotte.network.model.GoogleIdTokenRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: AuthApi,
    private val preferences: UserDataRepository,
    private val dao: FullRecipeDao,
    @ApplicationScope private val coroutineScope: CoroutineScope
) {

    private val googleSignInClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("431140586774-o1p6cnlk42macn41t4ld0t0bh2kr21fi.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()
    )

    val googleSignInIntent: Intent
        get() = googleSignInClient.signInIntent

    private val userDataFlow = preferences.userData


    val userCredentials = userDataFlow
        .map { userData ->
            UserCredentials(
                userData.userId,
                userData.token ?: return@map null
            )
        }

    val credentials = userCredentials.stateIn(coroutineScope, SharingStarted.Eagerly, null)

    private val authenticatedFlow = userDataFlow
        .map { userData -> userData.isConnected && userData.token?.isNotBlank() == true }

    val isAuthenticated =
        authenticatedFlow.stateIn(coroutineScope, SharingStarted.Eagerly, false)

    val user = userDataFlow.stateIn(coroutineScope, SharingStarted.Eagerly, null)

    val snackBarMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)

    val loginIsSuccessFull = MutableStateFlow<Boolean>(false)

    val authToken =  credentials.value?.token

    suspend fun loginByUserNamePassword(userNameOrMail: String?, password: String) {
        try {
            onAuthResponse(
                api.authUser(
                    AuthRequest(
                        userNameOrMail = userNameOrMail ?: "",
                        password = password
                    )
                )
            )

        } catch (e: Exception) {
            Log.d("debug_on_auth_response", "on_auth_response")
        }

    }


    suspend fun doGoogleLogin(signInTask: Task<GoogleSignInAccount>) {
        val account = signInTask.await()
        val idToken = account.idToken

        if (idToken.isNullOrEmpty()) {
            Log.e("debug_google", "ID token null ou vide")
            throw Exception()
        }
        val request = GoogleIdTokenRequest(idToken)
        val response = api.authGoogle(request)
        if (response.isSuccessful) {
            onAuthResponse(response)
        } else if (response.code() == 404) {
            val created = api.createGoogle(request)
            onAuthResponse(created)
        } else {
            throw UnknownError()
        }

    }

    suspend fun createUser(
        username: String,
        email: String,
        password: String,
        imageUri: Uri?,
    )  {
        val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val passwordPart = password.toRequestBody("text/plain".toMediaTypeOrNull())
        val imagePart = imageUri.toMultiPartBody(context)
        val response = api.createUser(
            username = usernamePart,
            email = emailPart,
            password = passwordPart,
            image = imagePart
        )
        onAuthResponse(response, true)
    }

    suspend fun updateUser(username: String?, imageUri: Uri?) {
        val token = "Bearer ${credentials.value?.token}"
        val userName = username?.toRequestBody("text/plain".toMediaTypeOrNull())
        val imagePart = imageUri.toMultiPartBody(context)

        val response = api.updateUserProfile(
            token = token,
            username = userName,
            image = imagePart
        )
        onAuthResponse(response, isUpdating = true)
    }


    suspend fun onAuthResponse(
        apiResponse: Response<AuthResponse>,
        isRegistering: Boolean = false,
        isUpdating: Boolean = false
    ) = withContext(Dispatchers.IO) {
        if (apiResponse.code() == 202 || apiResponse.code() == 200) {
            loginIsSuccessFull.value = true
            apiResponse.body()?.let { response ->
                preferences.updateUserInfo(
                    isConnected = true,
                    name = response.user.username!!,
                    userId = response.user.userId,
                    userToken = response.token,
                    userEmail = response.user.email ?: "",
                    userImage = response.user.image ?: ""
                )
                if (isRegistering) {
                    snackBarMessage.tryEmit("Welcome ${response.user.username}! Your account has been created successfully")
                } else if (isUpdating) {
                    snackBarMessage.tryEmit("Your account has been updated successfully")
                } else {
                    snackBarMessage.tryEmit("Welcome back ${response.user.username}")
                }
            }
        } else if (apiResponse.code() == 413) {
            loginIsSuccessFull.value = false
            snackBarMessage.tryEmit("Payload Too Large")
        } else if (apiResponse.code() == 409) {
            loginIsSuccessFull.value = false
            snackBarMessage.tryEmit("Your account can't be created : user already exists")
        }
        else {
            loginIsSuccessFull.value = false
            if (isRegistering) {
                snackBarMessage.tryEmit("Unknown error. Retry later")
               } else {
                snackBarMessage.tryEmit("Email/Password combination failed !")
            }
        }
    }


    suspend fun requestPasswordReset(email: String): Result<Unit> {
        return try {
            val response = api.requestPasswordReset(EmailRequest(email))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erreur : ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun deleteUser() {
        val credentials = credentials.value ?: return
        try {
            withContext(NonCancellable) {
                api.deleteUser(credentials.id)
                preferences.deleteUser()
                snackBarMessage.tryEmit("Your account has been deleted successfully")
                signOutGoogleUser()
                dao.deleteAllFavoritesRecipes()
            }
        } catch (e: Exception) {
            Log.d("debug_error_while_signing_out", "Error while signing out")
        }
    }

    suspend fun deleteAllUsers() {
        api.deleteAllUsers()
    }


    private suspend fun signOutGoogleUser() {
        googleSignInClient.signOut().await()
    }

    suspend fun logout() {
        try {
            withContext(NonCancellable) {
                if (isAuthenticated.value) {
                    snackBarMessage.tryEmit("You have been disconnected")
                } else {
                    snackBarMessage.tryEmit("You are not connected")
                }
                preferences.updateUserInfo(false)
                preferences.deleteFavoriteIds()
                // facebook - LoginManager.getInstance().logOut()
                // google
                signOutGoogleUser()
                dao.deleteAllFavoritesRecipes()

            }
        } catch (e: Exception) {
            Log.d("debug_error_while_signing_out", "Error while signing out")
        }
    }
}


data class UserCredentials(val id: Long, val token: String)