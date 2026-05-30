package com.francotte.domain

import com.francotte.data.interfaces.AuthRepository
import com.francotte.data.interfaces.LoginCredentials
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(credentials: LoginCredentials): Result<Unit> =
        authRepository.authenticate(credentials)
}
