package com.francotte.login

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.interfaces.EmailPasswordCredentials
import com.francotte.data.interfaces.GoogleCredentials
import com.francotte.domain.GetGoogleSignInIntentUseCase
import com.francotte.domain.LoginUseCase
import com.francotte.domain.RegisterUseCase
import com.francotte.domain.RequestPasswordResetUseCase
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    getGoogleSignInIntentUseCase: GetGoogleSignInIntentUseCase,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
) : ViewModel() {

    val loading = MutableStateFlow(false)

    val googleSignInIntent = getGoogleSignInIntentUseCase()

    private val _authSuccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val authSuccess: SharedFlow<Unit> = _authSuccess.asSharedFlow()

    val resetState = MutableStateFlow<Result<Unit>?>(null)

    fun requestReset(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            resetState.value = requestPasswordResetUseCase(email)
        }
    }

    fun doGoogleLogin(signInTask: Task<GoogleSignInAccount>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val idToken = signInTask.await().idToken
                if (idToken.isNullOrEmpty()) { onError(); return@launch }
                val result = loginUseCase(GoogleCredentials(idToken))
                if (result.isSuccess) onSuccess() else onError()
            } catch (e: Exception) {
                onError()
                Log.e("debug_google", "Erreur récupération compte Google", e)
            }
        }
    }

    fun createUserWithMailAndPassword(
        username: String,
        email: String,
        password: String,
        imageUri: Uri?,
    ) {
        viewModelScope.launch {
            val result = registerUseCase(username, email, password, imageUri)
            if (result.isSuccess) onSuccess() else onError()
        }
    }

    fun loginWithMailAndPassword(
        userNameOrMail: String?,
        password: String?,
    ) {
        if (!loading.value) {
            val nameOrMail = userNameOrMail?.takeUnless(CharSequence::isBlank) ?: return
            val pwd = password?.takeUnless(CharSequence::isBlank) ?: return
            viewModelScope.launch(Dispatchers.Default) {
                loading.value = true
                try {
                    val result = loginUseCase(EmailPasswordCredentials(nameOrMail, pwd))
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
        onCleared()
    }
}
