package com.francotte.login

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.auth.SessionRepository
import com.francotte.auth.strategy.EmailPasswordCredentials
import com.francotte.auth.strategy.GoogleCredentials
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val WEB_CLIENT_ID = "431140586774-o1p6cnlk42macn41t4ld0t0bh2kr21fi.apps.googleusercontent.com"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnLoginClick -> loginWithMailAndPassword(action.usernameOrMail, action.password)
            // Google login (needs an Activity) and pure navigation are handled by LoginRoute.
            LoginAction.OnGoogleLoginClick,
            LoginAction.OnRegisterClick,
            LoginAction.OnResetPasswordClick -> Unit
        }
    }

    fun doGoogleLogin(activityContext: Context) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val credentialManager = CredentialManager.create(activityContext)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val response = credentialManager.getCredential(activityContext, request)
                val credential = response.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val result = sessionRepository.login(GoogleCredentials(idToken))
                    if (result.isSuccess) onSuccess() else onError()
                } else {
                    onError()
                }
            } catch (e: GetCredentialCancellationException) {
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e("debug_google", "Erreur récupération compte Google", e)
                onError()
            }
        }
    }

    private fun loginWithMailAndPassword(userNameOrMail: String, password: String) {
        if (state.value.isLoading) return
        if (userNameOrMail.isBlank() || password.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val result = sessionRepository.login(EmailPasswordCredentials(userNameOrMail, password))
                if (result.isSuccess) onSuccess() else onError()
            } catch (e: Exception) {
                Log.d("debug_email", "$e")
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun onSuccess() {
        _state.update { it.copy(isLoading = false) }
        _events.send(LoginEvent.NavigateToFavorites)
    }

    private suspend fun onError() {
        _state.update { it.copy(isLoading = false) }
        _events.send(LoginEvent.ShowSnackbar("Google sign-in failed"))
    }
}

data class LoginState(
    val isLoading: Boolean = false,
)

sealed interface LoginAction {
    data class OnLoginClick(val usernameOrMail: String, val password: String) : LoginAction
    data object OnGoogleLoginClick : LoginAction
    data object OnRegisterClick : LoginAction
    data object OnResetPasswordClick : LoginAction
}

sealed interface LoginEvent {
    data object NavigateToFavorites : LoginEvent
    data class ShowSnackbar(val message: String) : LoginEvent
}
