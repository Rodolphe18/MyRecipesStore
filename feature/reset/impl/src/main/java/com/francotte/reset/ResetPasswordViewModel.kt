package com.francotte.reset

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.auth.PasswordResetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val passwordResetRepository: PasswordResetRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ResetPasswordState())
    val state = _state.asStateFlow()

    fun onAction(action: ResetPasswordAction) {
        when (action) {
            is ResetPasswordAction.OnConfirmClick -> resetPassword(action.token, action.newPassword)
        }
    }

    private fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            passwordResetRepository.resetPassword(token, newPassword).fold(
                onSuccess = {
                    _state.update { it.copy(isSuccess = true, message = "Password updated successfully") }
                },
                onFailure = { error ->
                    _state.update { it.copy(isSuccess = false, message = error.message ?: "Unknown error") }
                },
            )
        }
    }
}

@Immutable
data class ResetPasswordState(
    val message: String = "",
    val isSuccess: Boolean = false,
)

sealed interface ResetPasswordAction {
    data class OnConfirmClick(val token: String, val newPassword: String) : ResetPasswordAction
}
