package com.francotte.auth

import android.content.Context
import android.net.Uri
import com.francotte.auth.RegistrationRepository
import com.francotte.network.api.AuthApi
import com.francotte.network.utils.toMultiPartBody
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegistrationManager @Inject constructor(
    private val api: AuthApi,
    private val authSynchronizer: AuthSynchronizer,
    @param:ApplicationContext private val context: Context,
) : RegistrationRepository {

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        imageUri: Uri?,
    ): Result<Unit> =
        try {
            authSynchronizer.handle(
                api.createUser(
                    username = username.toRequestBody("text/plain".toMediaTypeOrNull()),
                    email = email.toRequestBody("text/plain".toMediaTypeOrNull()),
                    password = password.toRequestBody("text/plain".toMediaTypeOrNull()),
                    image = imageUri.toMultiPartBody(context),
                ),
                operation = AuthOperation.REGISTER,
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
}
