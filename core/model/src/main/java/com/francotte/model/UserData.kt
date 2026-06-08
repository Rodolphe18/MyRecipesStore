package com.francotte.model

import kotlin.Long

data class UserData(
    val userId: Long,
    val userName: String,
    val connectionMethod: ConnectionMethod,
    val email: String,
    val image: String,
    val isConnected: Boolean,
    val token: String? = null,
    val favoriteRecipesIds: Set<String>,
    val pendingFavorites: Map<String, Boolean>,
    val isPremium: Boolean = false,
    val launchCount: Int = 0,
    val hasRated: Boolean = false,
    val lastPromptLaunch: Int = 0,
) {
    val isAuthenticated: Boolean
        get() = isConnected && !token.isNullOrBlank()
}

enum class ConnectionMethod {
    EMAIL,
    FACEBOOK,
    GOOGLE,
}
