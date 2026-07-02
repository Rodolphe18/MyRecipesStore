package com.francotte.model

import androidx.compose.runtime.Immutable
import kotlin.Long

@Immutable
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

@Immutable
enum class ConnectionMethod {
    EMAIL,
    FACEBOOK,
    GOOGLE,
}

/** Placeholder URL the backend returns when the user has no profile picture. */
const val NO_PROFILE_IMAGE_URL = "https://app.myrecipesstore18.com/null"

/** True when the user has a real profile picture (not the backend placeholder). */
val UserData.hasCustomImage: Boolean
    get() = image.isNotBlank() && image != NO_PROFILE_IMAGE_URL
