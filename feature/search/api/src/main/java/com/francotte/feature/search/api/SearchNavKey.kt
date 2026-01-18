package com.francotte.feature.search.api

import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
object SearchNavKey : NavKey

fun Navigator.navigateToSearch() {
    navigate(SearchNavKey)
}
