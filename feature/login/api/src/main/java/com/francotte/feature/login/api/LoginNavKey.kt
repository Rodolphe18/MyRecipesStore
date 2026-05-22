package com.francotte.feature.login.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
object LoginNavKey : NavKey


fun Navigator.navigateToLogin() {
    navigate(LoginNavKey)
}

fun Navigator.navigateToLoginOnLogout() {
    state.topLevelStack.apply {
        clear()
        add(LoginNavKey)
    }
}
