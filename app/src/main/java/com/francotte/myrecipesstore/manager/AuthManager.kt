package com.francotte.myrecipesstore.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.facebook.login.LoginManager
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.database.dao.FullRecipeDao
import com.francotte.myrecipesstore.network.model.AuthRequest
import com.francotte.myrecipesstore.network.model.AuthResponse
import com.francotte.myrecipesstore.network.model.CurrentUser
import com.francotte.myrecipesstore.network.api.AuthApi
import com.francotte.myrecipesstore.datastore.UserDataSource
import com.francotte.myrecipesstore.network.model.EmailRequest
import com.francotte.myrecipesstore.network.model.GoogleIdTokenRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Singleton
    @Provides
    fun provideAuthManager(
        @ApplicationContext context: Context,
        api: AuthApi,
        preferences: UserDataSource,
        fullRecipeDao: FullRecipeDao
    ): AuthManager =
        AuthManager(context, api, preferences, fullRecipeDao)


}


@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: AuthApi,
    private val preferences: UserDataSource,
    private val dao: FullRecipeDao
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val googleSignInClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.google_server_client_id))
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
                userData.userInfo.user.id,
                userData.userInfo.token.takeUnless(CharSequence::isBlank) ?: return@map null
            )
        }

    val credentials = userCredentials.stateIn(coroutineScope, SharingStarted.Eagerly, null)

    private val authenticatedFlow = userDataFlow
        .map { userData -> userData.userInfo.connected && userData.userInfo.token.isNotBlank() }

    val isAuthenticated =
        authenticatedFlow.stateIn(coroutineScope, SharingStarted.Eagerly, false)

    val user = userDataFlow
        .map { userData ->
            userData.userInfo.user?.let { user ->
                CurrentUser(user)
            }
        }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    val snackBarMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)

    val loginIsSuccessFull = MutableStateFlow<Boolean>(false)

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


    fun doGoogleLogin(signInTask: Task<GoogleSignInAccount>) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val account = signInTask.await()
                val idToken = account.idToken

                if (idToken.isNullOrEmpty()) {
                    Log.e("debug_google", "ID token null ou vide")
                    return@launch
                }

                val request = GoogleIdTokenRequest(idToken)

                try {
                    val response = api.authGoogle(request)
                    if (response.isSuccessful) {
                        onAuthResponse(response)
                    } else if (response.code() == 404) {
                        val created = api.createGoogle(request)
                        onAuthResponse(created)
                    } else {
                        Log.e("debug_google", "Erreur auth : ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("debug_google", "Erreur API Google", e)
                }

            } catch (e: Exception) {
                Log.e("debug_google", "Erreur récupération compte Google", e)
            }
        }
    }

    suspend fun createUser(
        username: String,
        email: String,
        password: String,
        imageUri: Uri?,
    ) {
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
        apiResponse: Response<AuthResponse>, isRegistering: Boolean = false, isUpdating: Boolean=false) {
        Log.d("debug_auth", apiResponse.code().toString())
        Log.d("debug_auth1", apiResponse.message())
        Log.d("debug_auth2", apiResponse.isSuccessful.toString())
        if (apiResponse.code() == 202 || apiResponse.code() == 200) {
            apiResponse.body()?.let { response ->
                preferences.updateUserInfo(
                    isConnected = true,
                    name = response.user.username!!,
                    userId = response.user.userId,
                    userToken = response.token,
                    email = response.user.email ?: "",
                    image = response.user.image ?: ""
                )
                if (isRegistering) {
                    snackBarMessage.tryEmit("Welcome ${response.user.username}! Your account has been created successfully")
                } else if (isUpdating) {
                    snackBarMessage.tryEmit("Your account has been updated successfully")
                } else {
                    snackBarMessage.tryEmit("Welcome back ${response.user.username}")
                }
                loginIsSuccessFull.value = true
            }
        } else if (apiResponse.code() == 413) {
            snackBarMessage.tryEmit("Payload Too Large")
        } else {
            if (isRegistering) {
                snackBarMessage.tryEmit("Your account can't be created : user already exists")
            } else {
            snackBarMessage.tryEmit("Email/Password combination failed !")
        }}
    }


    suspend fun requestPasswordReset(email: String): Result<Unit> {
        return try {
            val response = api.requestPasswordReset(EmailRequest(email))
            Log.d("debug_reset_code", response.code().toString())
            Log.d("debug_reset_message", response.message().toString())
            if (response.isSuccessful) {
                Log.d("debug_reset_success", email)
                Result.success(Unit)
            } else {
                Log.d("debug_reset_failure", email)
                Result.failure(Exception("Erreur : ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun deleteUser() {
        val credentials = credentials.value ?: return
        api.deleteUser(credentials.id)
        logout()
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
                preferences.updateUserInfo(false)
                preferences.deleteFavoriteIds()
                // facebook
                LoginManager.getInstance().logOut()
                // google
                signOutGoogleUser()
                dao.deleteAllFavoritesRecipes()
                snackBarMessage.tryEmit("You have been disconnected")
            }
        } catch (e: Exception) {
            Log.d("debug_error_while_signing_out", "Error while signing out")
        }
    }
}


data class UserCredentials(val id: Long, val token: String)