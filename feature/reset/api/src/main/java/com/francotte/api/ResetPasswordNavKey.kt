package com.francotte.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordNavKey(val token: String): NavKey

fun Navigator.navigateToResetPassword(token: String) {
    navigate(ResetPasswordNavKey(token))
}
