package com.francotte.auth.strategy

interface LoginAuthStrategy {
    suspend fun tryAuthenticate(credentials: LoginCredentials): Result<Unit>?
}

internal interface LoginStrategy<C : LoginCredentials> : LoginAuthStrategy {
    fun toTypedOrNull(credentials: LoginCredentials): C?
    suspend fun authenticate(credentials: C): Result<Unit>

    override suspend fun tryAuthenticate(credentials: LoginCredentials): Result<Unit>? {
        val typedCredentials = toTypedOrNull(credentials) ?: return null
        return authenticate(typedCredentials)
    }
}
