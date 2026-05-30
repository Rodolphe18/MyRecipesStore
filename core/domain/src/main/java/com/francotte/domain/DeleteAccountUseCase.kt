package com.francotte.domain

import com.francotte.data.interfaces.AuthRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() = authRepository.deleteAccount()
}
