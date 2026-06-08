package com.francotte.auth.strategy

import com.francotte.auth.AuthSynchronizer
import com.francotte.network.api.AuthApi
import com.francotte.network.model.AuthRequest
import javax.inject.Inject

class EmailPasswordLoginStrategy @Inject constructor(
    private val api: AuthApi,
    private val authSynchronizer: AuthSynchronizer,
) : LoginStrategy<EmailPasswordCredentials> {

    override fun toTypedOrNull(credentials: LoginCredentials): EmailPasswordCredentials? =
        credentials as? EmailPasswordCredentials

    override suspend fun authenticate(credentials: EmailPasswordCredentials): Result<Unit> =
        try {
            authSynchronizer.handle(api.authUser(AuthRequest(credentials.userNameOrMail, credentials.password)))
        } catch (e: Exception) {
            Result.failure(e)
        }
}
