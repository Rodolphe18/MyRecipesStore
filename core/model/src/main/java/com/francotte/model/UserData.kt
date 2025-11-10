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
)

enum class ConnectionMethod {
    EMAIL, FACEBOOK, GOOGLE;
}



