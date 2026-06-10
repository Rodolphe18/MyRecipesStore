package com.francotte.register

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.auth.RegistrationRepository
import com.francotte.domain.RegisterValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registrationRepository: RegistrationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    private val _events = Channel<RegisterEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.OnNameChange -> _state.update { it.copy(name = action.name) }
            is RegisterAction.OnEmailChange -> _state.update { it.copy(email = action.email) }
            is RegisterAction.OnPasswordChange -> _state.update { it.copy(password = action.password) }
            is RegisterAction.OnConfirmPasswordChange -> _state.update { it.copy(confirmPassword = action.confirmPassword) }
            is RegisterAction.OnImageChange -> _state.update { it.copy(imageUri = action.uri) }
            RegisterAction.OnRegisterClick -> createUser()
            // Back is pure navigation, handled by RegisterRoute.
            RegisterAction.OnBackClick -> Unit
        }
    }

    private fun createUser() {
        val form = state.value
        if (!form.canRegister || form.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = registrationRepository.register(form.name, form.email, form.password, form.imageUri)
            _state.update { it.copy(isLoading = false) }
            if (result.isSuccess) {
                _events.send(RegisterEvent.NavigateToFavorites)
            } else {
                _events.send(RegisterEvent.ShowSnackbar("Registration failed"))
            }
        }
    }
}

data class RegisterState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
) {
    val isNameValid: Boolean
        get() = RegisterValidator.isValidName(name)
    val isEmailValid: Boolean
        get() = RegisterValidator.isValidEmail(email)
    val isPasswordValid: Boolean
        get() = RegisterValidator.isValidPassword(password)
    val isConfirmPasswordValid: Boolean
        get() = RegisterValidator.isPasswordConfirmed(password, confirmPassword)
    val canRegister: Boolean
        get() = isNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid
}

sealed interface RegisterAction {
    data class OnNameChange(val name: String) : RegisterAction
    data class OnEmailChange(val email: String) : RegisterAction
    data class OnPasswordChange(val password: String) : RegisterAction
    data class OnConfirmPasswordChange(val confirmPassword: String) : RegisterAction
    data class OnImageChange(val uri: Uri?) : RegisterAction
    data object OnRegisterClick : RegisterAction
    data object OnBackClick : RegisterAction
}

sealed interface RegisterEvent {
    data object NavigateToFavorites : RegisterEvent
    data class ShowSnackbar(val message: String) : RegisterEvent
}
