package com.francotte.data.manager

import com.francotte.data.interfaces.AuthStrategy
import com.francotte.data.interfaces.GoogleCredentials
import com.francotte.data.interfaces.LoginCredentials
import com.francotte.network.api.AuthApi
import com.francotte.network.model.GoogleIdTokenRequest
import javax.inject.Inject

class GoogleAuthStrategy @Inject constructor(
    private val api: AuthApi,
    private val processor: AuthResponseProcessor,
) : AuthStrategy<GoogleCredentials> {

    override fun toTypedOrNull(credentials: LoginCredentials): GoogleCredentials? =
        credentials as? GoogleCredentials

    override suspend fun authenticate(credentials: GoogleCredentials): Result<Unit> =
        try {
            val request = GoogleIdTokenRequest(credentials.idToken)
            val response = api.authGoogle(request)
            when {
                response.isSuccessful -> processor.handle(response)
                response.code() == 404 -> processor.handle(api.createGoogle(request))
                else -> Result.failure(Exception("Google auth failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}
