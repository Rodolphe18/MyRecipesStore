package com.francotte.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.domain.GetCurrentUserUseCase
import com.francotte.domain.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
) : ViewModel() {

    val user = getCurrentUserUseCase()

    fun setProfile(userName: String?, imageUri: Uri?) {
        viewModelScope.launch { updateProfileUseCase(userName, imageUri) }
    }
}
