package com.francotte.domain

import android.net.Uri
import com.francotte.data.interfaces.AuthRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(userName: String?, imageUri: Uri?) =
        authRepository.updateUser(userName, imageUri)
}
