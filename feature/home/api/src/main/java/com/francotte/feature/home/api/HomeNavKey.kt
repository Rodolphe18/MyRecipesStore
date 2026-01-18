package com.francotte.feature.home.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
object HomeNavKey : NavKey

fun Navigator.navigateToHome() {
    navigate(HomeNavKey)
}
