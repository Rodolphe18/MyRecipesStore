package com.francotte.myrecipesstore.ui.compose.favorites.login

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.francotte.myrecipesstore.auth.AuthManager
import com.francotte.myrecipesstore.model.AuthRequest
import com.francotte.myrecipesstore.model.Provider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val authManager: AuthManager, handle: SavedStateHandle) : ViewModel() {

    val loading = MutableStateFlow(false)

    // Facebook
    val facebookLoginManager: LoginManager = LoginManager.getInstance()
    private val facebookCallbackManager: CallbackManager = CallbackManager.Factory.create()
    private val facebookCallback = object : FacebookCallback<LoginResult> {
        override fun onSuccess(result: LoginResult) {
            doFacebookLogin(result)
        }
        override fun onCancel() {
            loading.value = false
        }
        override fun onError(error: FacebookException) {
            viewModelScope.launch {
                Log.d("debug_facebook_callback", "error")
            }
        }
    }
    val startFacebookLoginEvent = MutableSharedFlow<List<String>>()

    // Google
    val googleSignInIntent = authManager.googleSignInIntent

    val openForgottenPasswordEvent = MutableSharedFlow<Unit>()
    val openRegisterEvent = MutableSharedFlow<Unit>()
   // val askProviderRegistrationEvent = MutableSharedFlow<ProviderData>()
   // val openProviderRegistrationEvent = MutableSharedFlow<ProviderData>()
    val loginFinishedEvent = MutableSharedFlow<Boolean>()
    val closeKeyboardEvent = MutableSharedFlow<Unit>()

    val authSuccess = MutableStateFlow(false)


    fun createUser(authRequest: AuthRequest) {
        viewModelScope.launch {
            loading.value = true
            authManager.createUser(authRequest)
        }
    }

    fun loginWithFacebook() {
        if (!loading.value) {
            viewModelScope.launch {
                loading.value = true
                facebookLoginManager.registerCallback(facebookCallbackManager, facebookCallback)
                val recoveredToken = try {
                    authManager.recoverFacebookToken()
                } catch (e: Exception) {
                    Log.d("Unable to recover Facebook token", "")
                    null
                }
                if (recoveredToken == null) {
                    startFacebookLoginEvent.emit(listOf("public_profile", "email"))
                } else {
                    onFacebookSuccess(recoveredToken)
                }
                loading.value = false
            }
        }
    }

   fun deleteUser() {
        viewModelScope.launch {
            authManager.deleteUser()
        }
    }


    private fun doFacebookLogin(result: LoginResult) {
        viewModelScope.launch {
            onFacebookSuccess(result.accessToken)
        }
    }

    private suspend fun onFacebookSuccess(token: AccessToken) {
        try {
            authManager.loginByFacebook(token)
            onSuccess(Provider.FACEBOOK)
        } catch (e: Exception) {
            Log.d("debug_facebook", "error")
        }
    }


    fun doGoogleLogin(signInTask: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            try {
                val account = signInTask.await()
                try {
                    authManager.loginByGoogle(account)
                    onSuccess(Provider.GOOGLE)
                } catch (e: Exception) {
                    Log.d("debug_google", "error")
                }
            } catch (e: Exception) {
                Log.d("debug_google", "error")
            }
        }
    }

    fun loginWithMailAndPassword(email: String?, password: String?) {
        if (!loading.value) {
          val mail = email?.takeUnless(CharSequence::isBlank) ?: return
           val pwd = password?.takeUnless(CharSequence::isBlank) ?: return
            viewModelScope.launch {
                loading.value = true
                try {
                    authManager.loginByEmailPassword(mail, pwd)
                    onSuccess(Provider.EMAIL)
                } catch (e: Exception) {
                    Log.d("debug_email", "error")
                }
            }
        }
    }

    fun openRegister() {
        viewModelScope.launch {
            if (!loading.value) {
                openRegisterEvent.emit(Unit)
            }
        }
    }

//    fun doProviderRegistration(providerData: ProviderData) {
//        viewModelScope.launch {
//            openProviderRegistrationEvent.emit(providerData)
//        }
//    }

    private suspend fun onSuccess(provider: Provider) {
        facebookLoginManager.unregisterCallback(facebookCallbackManager)
        loading.value = false
        authSuccess.value = true
    }


    override fun onCleared() {
        super.onCleared()
        facebookLoginManager.unregisterCallback(facebookCallbackManager)
    }

    companion object {
        private const val EMAIL = "email"
        private const val PASSWORD = "password"
        const val TITLE = "title"
        const val PROVIDER_DATA = "providerData"
    }
}