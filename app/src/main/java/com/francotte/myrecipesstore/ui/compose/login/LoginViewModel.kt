package com.francotte.myrecipesstore.ui.compose.login

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.myrecipesstore.manager.AuthManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    val loading = MutableStateFlow(false)

    // Google
    val googleSignInIntent = authManager.googleSignInIntent

    val authSuccess = MutableStateFlow(false)

    val resetState = MutableStateFlow<Result<Unit>?>(null)

    fun requestReset(email: String) {
        viewModelScope.launch {
           resetState.value = authManager.requestPasswordReset(email)
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            authManager.deleteUser()
        }
    }

    fun deleteAllUsers() {
        viewModelScope.launch {
            authManager.deleteAllUsers()
        }
    }

    fun doGoogleLogin(signInTask: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            try {
                authManager.doGoogleLogin(signInTask)
                onSuccess()
            } catch (e: Exception) {
                Log.e("debug_google", "Erreur récupération compte Google", e)
            }
        }
    }

    fun createUserWithMailAndPassword(username: String,
                                      email: String,
                                      password: String,
                                      imageUri: Uri?) {
        viewModelScope.launch {
            authManager.createUser(username,email,password,imageUri)
            if (authManager.loginIsSuccessFull.value) onSuccess() else onError()
        }
    }

    fun loginWithMailAndPassword(userNameOrMail: String?, password: String?) {
        if (!loading.value) {
            val nameOrMail = userNameOrMail?.takeUnless(CharSequence::isBlank) ?: return
           val pwd = password?.takeUnless(CharSequence::isBlank) ?: return
            viewModelScope.launch {
                loading.value = true
                try {
                    authManager.loginByUserNamePassword(nameOrMail, pwd)
                    if (authManager.loginIsSuccessFull.value) onSuccess() else onError()
                } catch (e: Exception) {
                    Log.d("debug_email", "$e")
                }
            }
        }
    }


    fun onSuccess() {
        loading.value = false
        authSuccess.value = true
    }

    private fun onError() {
        loading.value = false
        authSuccess.value = false
        onCleared()
    }

}