package com.francotte.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
object RequestResetNavKey: NavKey

fun Navigator.navigateToRequestReset() {
    navigate(RequestResetNavKey)
}
