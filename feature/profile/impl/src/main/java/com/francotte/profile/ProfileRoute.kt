package com.francotte.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.ProfileNavKey
import com.francotte.common.counters.ScreenCounter
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

const val PROFILE_ROUTE = "profile_route"



fun EntryProviderScope<NavKey>.profileEntry(navigator: Navigator) {
    entry<ProfileNavKey> {
        ProfileRoute(onBackPressed = navigator::goBack)
    }
}

fun NavController.navigateToProfileScreen(navOptions: NavOptions? = null) {
    this.navigate(PROFILE_ROUTE, navOptions)
}

fun NavGraphBuilder.profileScreen(onBackPressed: () -> Unit) {
    composable(route = PROFILE_ROUTE) {
        ProfileRoute(onBackPressed = onBackPressed)
    }
}

@Composable
fun ProfileRoute(
    onBackPressed: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val currentUser by viewModel.user.collectAsStateWithLifecycle()
    currentUser?.let { userData ->
        ProfileScreen(onBackPressed, userData) { name, image -> viewModel.setProfile(name, image) }
    }
    ScreenCounter.increment()
}
