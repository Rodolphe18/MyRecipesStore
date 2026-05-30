package com.francotte.domain

import com.francotte.data.interfaces.AuthRepository
import com.francotte.model.UserData
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): StateFlow<UserData?> = authRepository.user
}
