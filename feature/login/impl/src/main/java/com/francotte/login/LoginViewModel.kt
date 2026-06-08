package com.francotte.login

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.auth.RegistrationRepository
import com.francotte.auth.SessionRepository
import com.francotte.auth.strategy.EmailPasswordCredentials
import com.francotte.auth.strategy.GoogleCredentials
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val WEB_CLIENT_ID = "431140586774-o1p6cnlk42macn41t4ld0t0bh2kr21fi.apps.googleusercontent.com"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val registrationRepository: RegistrationRepository,
) : ViewModel() {

    val loading = MutableStateFlow(false)

    private val _authSuccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val authSuccess: SharedFlow<Unit> = _authSuccess.asSharedFlow()

    private val _authError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val authError: SharedFlow<String> = _authError.asSharedFlow()

    val resetState = MutableStateFlow<Result<Unit>?>(null)

    fun requestReset(email: String) {
        viewModelScope.launch {
            resetState.value = registrationRepository.requestPasswordReset(email)
        }
    }

    fun doGoogleLogin(activityContext: Context) {
        viewModelScope.launch {
            loading.value = true
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
                loading.value = false
            } catch (e: Exception) {
                Log.e("debug_google", "Erreur récupération compte Google", e)
                onError()
            }
        }
    }

    fun loginWithMailAndPassword(
        userNameOrMail: String?,
        password: String?,
    ) {
        if (!loading.value) {
            val nameOrMail = userNameOrMail?.takeUnless(CharSequence::isBlank) ?: return
            val pwd = password?.takeUnless(CharSequence::isBlank) ?: return
            viewModelScope.launch {
                loading.value = true
                try {
                    val result = sessionRepository.login(EmailPasswordCredentials(nameOrMail, pwd))
                    if (result.isSuccess) onSuccess() else onError()
                } catch (e: Exception) {
                    Log.d("debug_email", "$e")
                }
            }
        }
    }

    fun onSuccess() {
        loading.value = false
        _authSuccess.tryEmit(Unit)
    }

    private fun onError() {
        loading.value = false
        _authError.tryEmit("Google sign-in failed")
    }
}
