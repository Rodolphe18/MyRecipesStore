package com.francotte.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.francotte.common.ScreenCounter


const val PROFILE_ROUTE = "profile_route"

fun NavController.navigateToProfileScreen(navOptions: NavOptions? = null) {
    this.navigate(PROFILE_ROUTE, navOptions)
}

fun NavGraphBuilder.profileScreen(onBackPressed: () -> Unit) {
    composable(route = PROFILE_ROUTE) {
        ProfileRoute(onBackPressed = onBackPressed)
    }
}

@Composable
fun ProfileRoute(onBackPressed: () -> Unit, viewModel: ProfileViewModel= hiltViewModel()) {
    val currentUser by viewModel.user.collectAsStateWithLifecycle()
    currentUser?.let {userData ->
        ProfileScreen(onBackPressed, userData) { name, image -> viewModel.setProfile(name, image) }

    }
    ScreenCounter.increment()
}

