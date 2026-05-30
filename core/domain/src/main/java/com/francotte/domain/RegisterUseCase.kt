package com.francotte.domain

import android.net.Uri
import com.francotte.data.interfaces.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(username: String, email: String, password: String, imageUri: Uri?): Result<Unit> =
        authRepository.register(username, email, password, imageUri)
}
