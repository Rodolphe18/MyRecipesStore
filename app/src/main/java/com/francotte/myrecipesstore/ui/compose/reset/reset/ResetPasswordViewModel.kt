package com.francotte.myrecipesstore.ui.compose.reset.reset

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.myrecipesstore.network.api.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val api: AuthApi
) : ViewModel() {

    var uiState by mutableStateOf("")
    var isSuccess by mutableStateOf(false)

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val response = api.resetPassword(mapOf(
                    "token" to token,
                    "newPassword" to newPassword
                ))
                if (response.isSuccessful) {
                    isSuccess = true
                    uiState = "Mot de passe mis à jour avec succès."
                } else {
                    isSuccess = false
                    uiState = response.errorBody()?.string() ?: "Erreur inconnue."
                }
            } catch (e: Exception) {
                isSuccess = false
                uiState = "Erreur : ${e.localizedMessage}"
            }
        }
    }
}
