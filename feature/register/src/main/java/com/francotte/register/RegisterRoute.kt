package com.francotte.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.francotte.common.counters.ScreenCounter
import com.francotte.login.LoginViewModel


const val REGISTER_ROUTE = "register_route"

fun NavController.navigateToRegisterScreen(navOptions: NavOptions? = null) {
    this.navigate(REGISTER_ROUTE, navOptions)
}

fun NavGraphBuilder.registerScreen(onBackPressed: () -> Unit,navigateToFavoriteScreen: () ->Unit) {
    composable(route = REGISTER_ROUTE) {
        RegisterRoute(onBackPressed = onBackPressed, navigateToFavoriteScreen = { navigateToFavoriteScreen() })
    }
}

@Composable
fun RegisterRoute(onBackPressed:()->Unit, navigateToFavoriteScreen: () ->Unit, viewModel: LoginViewModel = hiltViewModel()) {
    val authSuccess by viewModel.authSuccess.collectAsStateWithLifecycle()
    RegisterScreen(onBackPressed, viewModel)
    if (authSuccess) navigateToFavoriteScreen()
    ScreenCounter.increment()
}