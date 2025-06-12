package com.francotte.myrecipesstore.manager

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import com.facebook.AccessToken
import com.facebook.LoginStatusCallback
import com.facebook.login.LoginManager
import com.francotte.myrecipesstore.database.dao.FullRecipeDao
import com.francotte.myrecipesstore.network.model.AuthRequest
import com.francotte.myrecipesstore.network.model.AuthResponse
import com.francotte.myrecipesstore.network.model.CurrentUser
import com.francotte.myrecipesstore.network.model.Provider
import com.francotte.myrecipesstore.network.api.AuthApi
import com.francotte.myrecipesstore.protobuf.User
import com.francotte.myrecipesstore.datastore.UserDataSource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
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
    fun provideAuthManager(@ApplicationContext context: Context, api: AuthApi,preferences: UserDataSource, fullRecipeDao: FullRecipeDao): AuthManager =
        AuthManagerImpl(context, api,preferences,fullRecipeDao)
}

interface AuthManager {
    val googleSignInIntent:Intent
    val credentials:StateFlow<UserCredentials?>
    val isAuthenticated:StateFlow<Boolean>
    suspend fun createGoogle(account: GoogleSignInAccount)
    suspend fun createUser(authRequest: AuthRequest)
    suspend fun createFacebook(token: String)
    suspend fun loginByEmailPassword(email: String, password: String)
    suspend fun loginByFacebook(token: AccessToken)
    suspend fun recoverFacebookToken(): AccessToken?
    suspend fun loginByGoogle(account: GoogleSignInAccount)
    suspend fun deleteUser()
    suspend fun logout()
}


@Suppress("TooManyFunctions")
class AuthManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: AuthApi,
    private val preferences: UserDataSource,
    private val dao: FullRecipeDao
) : AuthManager {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val googleSignInClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            //  .requestServerAuthCode(context.getString(R.string.google_server_client_id))
            .build()
    )

    override val googleSignInIntent: Intent
        get() = googleSignInClient.signInIntent

    private val userDataFlow = preferences.userData


    val userCredentials = userDataFlow
        .map { userData ->
            UserCredentials(
                userData.userInfo.user.id ?: return@map null,
                userData.userInfo.token.takeUnless(CharSequence::isBlank) ?: return@map null
            )
        }

    override val credentials = userCredentials.stateIn(coroutineScope, SharingStarted.Eagerly, null)

    private val authenticatedFlow = userDataFlow
        .map { userData -> userData.userInfo.connected && userData.userInfo.token.isNotBlank() }

    override val isAuthenticated =
        authenticatedFlow.stateIn(coroutineScope, SharingStarted.Eagerly, false)

    val user = userDataFlow
        .map { userData ->
            userData.userInfo.user?.let { user ->
                CurrentUser(user)
            }
        }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)


    suspend fun checkAuthenticated(): Boolean {
        return authenticatedFlow.firstOrNull() == true
    }

    override suspend fun loginByEmailPassword(email: String, password: String) {
        try {
            Log.d("debug_login5", "5")
            onAuthResponse(
                Provider.EMAIL,
                api.authenticate(AuthRequest(username = email, password = password))
            )
            Log.d("debug_login6", "6")
        } catch (e: Exception) {
            Log.d("debug_on_auth_response", "on_auth_response")
        }
    }

    override suspend fun recoverFacebookToken(): AccessToken? {
        // Check if user is still logged-in
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

    override suspend fun loginByFacebook(token: AccessToken) {
        //   onAuthResponse(Provider.FACEBOOK, api.authenticateWithFacebook(token.token.toRequestBody()))
    }

    override suspend fun loginByGoogle(account: GoogleSignInAccount) {
        //   onAuthResponse(Provider.GOOGLE, api.authenticateWithGoogle(requireNotNull(account.serverAuthCode).toRequestBody()))
    }

    suspend fun trySilentReconnect(): Boolean {
        // Find user connection method
        val method = userDataFlow.firstOrNull()?.userInfo?.user?.method
        // We only can try for FB connect
        if (method != User.ConnectionMethod.FACEBOOK) return false
        val fbToken = recoverFacebookToken() ?: return false
        return runCatching { loginByFacebook(fbToken) }.isSuccess
    }

    override suspend fun createFacebook(token: String) {
//        onAuthResponse(
//            Provider.FACEBOOK,
//             api.createFacebook(token.toRequestBody())
//        )
    }

    override suspend fun createGoogle(account: GoogleSignInAccount) {
        createGoogle(requireNotNull(account.serverAuthCode))
    }

    suspend fun createGoogle(authCode: String) {
//        onAuthResponse(
//            Provider.GOOGLE,
//            api.createGoogle(authCode.toRequestBody())
//        )
    }

    override suspend fun createUser(authRequest: AuthRequest) {
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
        if (apiResponse.isSuccessful && apiResponse.code() == 202) {

            apiResponse.body()?.let { response ->
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


    override suspend fun deleteUser() {
        val credentials = credentials.value ?: return
        api.deleteUser(credentials.id)
        logout()
    }


    suspend fun signOutGoogleUser() {
        googleSignInClient.signOut().await()
    }

   override suspend fun logout() {
        try {
            withContext(NonCancellable) {
                preferences.updateUserInfo(false)
                // facebook
                LoginManager.getInstance().logOut()
                // google
                signOutGoogleUser()
                dao.deleteAllFavoritesRecipes()
            }
        } catch (e: Exception) {
            Log.d("debug_error_while_signing_out", "Error while signing out")
        }
    }
}

@Parcelize
data class UserCredentials(val id: Long, val token: String) : Parcelable