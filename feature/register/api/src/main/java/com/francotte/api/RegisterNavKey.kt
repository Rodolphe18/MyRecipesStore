package com.francotte.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object RegisterNavKey : NavKey

fun Navigator.navigateToRegister() {
    navigate(RegisterNavKey)
}
