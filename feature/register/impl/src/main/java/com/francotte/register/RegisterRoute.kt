package com.francotte.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.RegisterNavKey
import com.francotte.api.navigateToFavorites
import com.francotte.common.counters.ScreenCounter
import com.francotte.login.LoginViewModel
import com.francotte.navigation.Navigator

const val REGISTER_ROUTE = "register_route"


fun EntryProviderScope<NavKey>.registerEntry(navigator: Navigator) {
    entry<RegisterNavKey> {
        RegisterRoute(
            onBackPressed = navigator::goBack,
            navigateToFavoriteScreen = navigator::navigateToFavorites
        )
    }
}

fun NavController.navigateToRegisterScreen(navOptions: NavOptions? = null) {
    this.navigate(REGISTER_ROUTE, navOptions)
}

fun NavGraphBuilder.registerScreen(
    onBackPressed: () -> Unit,
    navigateToFavoriteScreen: () -> Unit,
) {
    composable(route = REGISTER_ROUTE) {
        RegisterRoute(
            onBackPressed = onBackPressed,
            navigateToFavoriteScreen = { navigateToFavoriteScreen() })
    }
}

@Composable
fun RegisterRoute(
    onBackPressed: () -> Unit,
    navigateToFavoriteScreen: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    RegisterScreen(onBackPressed, viewModel)
    LaunchedEffect(Unit) {
        viewModel.authSuccess.collect {
            navigateToFavoriteScreen()
        }
    }
    ScreenCounter.increment()
}
