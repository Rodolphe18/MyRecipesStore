package com.francotte.register

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.auth.RegistrationRepository
import com.francotte.domain.RegisterValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val imageUri: Uri? = null,
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

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registrationRepository: RegistrationRepository,
) : ViewModel() {

    val loading = MutableStateFlow(false)

    private val _authSuccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val authSuccess: SharedFlow<Unit> = _authSuccess.asSharedFlow()

    private val _authError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val authError: SharedFlow<String> = _authError.asSharedFlow()

    private val _formState = MutableStateFlow(RegisterFormState())
    val formState: StateFlow<RegisterFormState> = _formState.asStateFlow()

    fun onNameChange(name: String) = _formState.update { it.copy(name = name) }
    fun onEmailChange(email: String) = _formState.update { it.copy(email = email) }
    fun onPasswordChange(password: String) = _formState.update { it.copy(password = password) }
    fun onConfirmPasswordChange(confirmPassword: String) = _formState.update { it.copy(confirmPassword = confirmPassword) }
    fun onImageChange(uri: Uri?) = _formState.update { it.copy(imageUri = uri) }

    fun createUser() {
        val form = _formState.value
        if (!form.canRegister) return
        viewModelScope.launch {
            loading.value = true
            val result = registrationRepository.register(form.name, form.email, form.password, form.imageUri)
            if (result.isSuccess) onSuccess() else onError()
        }
    }

    private fun onSuccess() {
        loading.value = false
        _authSuccess.tryEmit(Unit)
    }

    private fun onError() {
        loading.value = false
        _authError.tryEmit("Registration failed")
    }
}
