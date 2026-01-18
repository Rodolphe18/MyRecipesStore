package com.francotte.feature.settings.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
object PremiumNavKey : NavKey

fun Navigator.navigateToPremium() {
    navigate(PremiumNavKey)
}

