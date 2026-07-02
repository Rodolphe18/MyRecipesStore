package com.francotte.profile

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.auth.SessionRepository
import com.francotte.data.interfaces.UserDataRepository
import com.francotte.domain.userNameValidator
import com.francotte.model.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    // null = not edited yet → the field mirrors the loaded user's name.
    private val editedName = MutableStateFlow<String?>(null)
    private val editedImage = MutableStateFlow<Uri?>(null)

    val state: StateFlow<ProfileState> = combine(
        userDataRepository.userData,
        editedName,
        editedImage,
    ) { user, name, image ->
        ProfileState(
            user = user,
            editedName = name ?: user.userName,
            editedImageUri = image,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileState())

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.OnNameChange -> editedName.value = action.name
            is ProfileAction.OnImageChange -> editedImage.value = action.uri
            ProfileAction.OnSave -> save()
            // Back is pure navigation, handled by ProfileRoute.
            ProfileAction.OnBackClick -> Unit
        }
    }

    private fun save() {
        val current = state.value
        val user = current.user ?: return
        // Keep the current name when the edited one is left blank.
        val finalName = current.editedName.ifBlank { user.userName }
        viewModelScope.launch {
            sessionRepository.updateUserInfo(finalName, current.editedImageUri)
        }
        // Reset edits; the name re-mirrors the (updated) user on the next emission.
        editedName.value = null
        editedImage.value = null
    }
}

@Immutable
data class ProfileState(
    val user: UserData? = null,
    val editedName: String = "",
    val editedImageUri: Uri? = null,
) {
    val isNameValid: Boolean
        get() = userNameValidator.isValid(editedName)

    val isNameChanged: Boolean
        get() = user != null && editedName != user.userName
}

@Immutable
sealed interface ProfileAction {
    data class OnNameChange(val name: String) : ProfileAction
    data class OnImageChange(val uri: Uri?) : ProfileAction
    data object OnSave : ProfileAction
    data object OnBackClick : ProfileAction
}
