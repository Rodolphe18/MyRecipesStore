package com.francotte.myrecipesstore.manager

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import com.facebook.AccessToken
import com.facebook.LoginStatusCallback
import com.facebook.login.LoginManager
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.database.dao.FullRecipeDao
import com.francotte.myrecipesstore.network.model.AuthRequest
import com.francotte.myrecipesstore.network.model.AuthResponse
import com.francotte.myrecipesstore.network.model.CurrentUser
import com.francotte.myrecipesstore.network.model.Provider
import com.francotte.myrecipesstore.network.api.AuthApi
import com.francotte.myrecipesstore.protobuf.User
import com.francotte.myrecipesstore.datastore.UserDataSource
import com.francotte.myrecipesstore.network.model.FacebookAccessTokenRequest
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
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


    suspend fun loginByEmailPassword(email: String, password: String) {
        try {

            onAuthResponse(
                Provider.EMAIL,
                api.authUser(AuthRequest(username = email, password = password))
            )
            snackBarMessage.tryEmit("Welcome back $email")
        } catch (e: Exception) {
            Log.d("debug_on_auth_response", "on_auth_response")
        }
    }

    suspend fun recoverFacebookToken(): AccessToken? {
        val recoveredAccessToken =
            AccessToken.getCurrentAccessToken()?.takeUnless(AccessToken::isExpired) ?: try {
                suspendCoroutine { cont ->
                    LoginManager.getInstance()
                        .retrieveLoginStatus(context, object : LoginStatusCallback {
                            override fun onCompleted(accessToken: AccessToken) {
                                cont.resume(accessToken)
                            }

                            override fun onFailure() {
                                cont.resume(null)
                            }

                            override fun onError(exception: Exception) {
                                cont.resumeWithException(exception)
                            }
                        })
                }
            } catch (e: Exception) {
                Log.d("", "Error while retrieving login status")
                null
            }
        // Only use token if it has the correct permissions
        return recoveredAccessToken?.takeIf {
            it.permissions.containsAll(
                listOf(
                    "public_profile",
                    "email"
                )
            )
        }
    }

    suspend fun loginByFacebook(token: AccessToken) {
        onAuthResponse(Provider.FACEBOOK, api.authFacebook(FacebookAccessTokenRequest(token.token)))
    }

    suspend fun loginByGoogle(account: GoogleSignInAccount) {
        onAuthResponse(
            Provider.GOOGLE,
            api.authGoogle(GoogleIdTokenRequest(requireNotNull(account.idToken)))
        )
    }

    suspend fun createFacebook(token: String) {
        onAuthResponse(
            Provider.FACEBOOK,
            api.createFacebook(FacebookAccessTokenRequest(token))
        )
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
                    // Étape 1 : essayer de connecter l'utilisateur existant
                    val response = api.authGoogle(request)
                    Log.d("debug_google_token", api.authGoogle(request).body()?.token.toString())
                    Log.d("debug_google_username", api.authGoogle(request).body()?.user?.username.toString())
                    Log.d("debug_google_userID", api.authGoogle(request).body()?.user?.userId.toString())
                    if (response.isSuccessful) {
                        onAuthResponse(Provider.GOOGLE, response)
                        Log.d("debug_google", "Connexion OK (utilisateur existant)")

                    } else if (response.code() == 404) {
                        // Étape 2 : utilisateur inconnu, on le crée
                        Log.d("debug_google", "Utilisateur non trouvé, tentative de création")
                        val created = api.createGoogle(request)
                        onAuthResponse(Provider.GOOGLE, created)
                        Log.d("debug_google", "Utilisateur créé")

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

    suspend fun createUser(authRequest: AuthRequest) {
        val response = api.createUser(authRequest)
        if (response.user.username != null) {
            preferences.updateUserInfo(
                isConnected = true,
                name = response.user.username,
                userId = response.user.userId,
                userToken = response.token
            )
        }
    }


    private suspend fun onAuthResponse(
        provider: Provider,
        apiResponse: Response<AuthResponse>
    ) {
        Log.d("debug_api_response_code", apiResponse.code().toString())
        Log.d("debug_api_response_success", apiResponse.isSuccessful.toString())
        if (apiResponse.isSuccessful && apiResponse.code() == 202 || apiResponse.code() == 200) {

            apiResponse.body()?.let { response ->
                Log.d("debug_api_response", response.toString())
                preferences.updateUserInfo(
                    isConnected = true,
                    name = response.user.username!!,
                    userId = response.user.userId,
                    userToken = response.token
                )
            }
        }
    }

    suspend fun resetPassword(email: String) {
        try {
            // api.resetPassword(email.toRequestBody())
        } catch (e: Exception) {
            Log.d("Unable to reset password", "Unable to reset password")
        }
    }


    suspend fun deleteUser() {
        val credentials = credentials.value ?: return
        api.deleteUser(credentials.id)
        logout()
    }


    private suspend fun signOutGoogleUser() {
        googleSignInClient.signOut().await()
    }

    suspend fun logout() {
        try {
            withContext(NonCancellable) {
                preferences.updateUserInfo(false)
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