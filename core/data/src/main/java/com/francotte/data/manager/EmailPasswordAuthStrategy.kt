package com.francotte.data.manager

import com.francotte.data.interfaces.AuthStrategy
import com.francotte.data.interfaces.EmailPasswordCredentials
import com.francotte.data.interfaces.LoginCredentials
import com.francotte.network.api.AuthApi
import com.francotte.network.model.AuthRequest
import javax.inject.Inject

class EmailPasswordAuthStrategy @Inject constructor(
    private val api: AuthApi,
    private val processor: AuthResponseProcessor,
) : AuthStrategy<EmailPasswordCredentials> {

    override fun toTypedOrNull(credentials: LoginCredentials): EmailPasswordCredentials? =
        credentials as? EmailPasswordCredentials

    override suspend fun authenticate(credentials: EmailPasswordCredentials): Result<Unit> =
        try {
            processor.handle(api.authUser(AuthRequest(credentials.userNameOrMail, credentials.password)))
        } catch (e: Exception) {
            Result.failure(e)
        }
}
