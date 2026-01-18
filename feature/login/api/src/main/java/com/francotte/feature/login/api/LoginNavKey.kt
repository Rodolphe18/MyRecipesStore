package com.francotte.feature.login.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
object LoginNavKey : NavKey


fun Navigator.navigateToLogin() {
    navigate(LoginNavKey)
}
