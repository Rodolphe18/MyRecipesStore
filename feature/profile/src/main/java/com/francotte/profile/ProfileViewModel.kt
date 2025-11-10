package com.francotte.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.domain.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    val user = authManager.user

    fun setProfile(userName:String?, imageUri:Uri?) {
        viewModelScope.launch {
            authManager.updateUser(userName, imageUri)
        }
    }

}