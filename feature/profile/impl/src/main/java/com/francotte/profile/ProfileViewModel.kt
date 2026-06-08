package com.francotte.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.auth.SessionRepository
import com.francotte.data.interfaces.UserDataRepository
import com.francotte.model.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    val user: StateFlow<UserData?> = userDataRepository.userData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setProfile(userName: String?, imageUri: Uri?) {
        viewModelScope.launch { sessionRepository.updateUserInfo(userName, imageUri) }
    }
}
