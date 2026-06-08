package com.francotte.auth.strategy

import com.francotte.auth.AuthSynchronizer
import com.francotte.network.api.AuthApi
import com.francotte.network.model.GoogleIdTokenRequest
import javax.inject.Inject

class GoogleLoginStrategy @Inject constructor(
    private val api: AuthApi,
    private val authSynchronizer: AuthSynchronizer,
) : LoginStrategy<GoogleCredentials> {

    override fun toTypedOrNull(credentials: LoginCredentials): GoogleCredentials? =
        credentials as? GoogleCredentials

    override suspend fun authenticate(credentials: GoogleCredentials): Result<Unit> =
        try {
            val request = GoogleIdTokenRequest(credentials.idToken)
            val response = api.authGoogle(request)
            when {
                response.isSuccessful -> authSynchronizer.handle(response)
                response.code() == 404 -> authSynchronizer.handle(api.createGoogle(request))
                else -> Result.failure(Exception("Google auth failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}
