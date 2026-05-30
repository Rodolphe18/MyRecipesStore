package com.francotte.settings

import androidx.lifecycle.ViewModel
import com.francotte.domain.DeleteAccountUseCase
import com.francotte.domain.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
) : ViewModel() {

    suspend fun logout() = logoutUseCase()

    suspend fun deleteAccount() = deleteAccountUseCase()
}
