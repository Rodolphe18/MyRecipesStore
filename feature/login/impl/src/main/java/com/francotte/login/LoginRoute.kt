package com.francotte.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.navigateToFavorites
import com.francotte.api.navigateToRegister
import com.francotte.api.navigateToRequestReset
import com.francotte.common.counters.ScreenCounter
import com.francotte.feature.login.api.LoginNavKey
import com.francotte.navigation.Navigator

const val LOGIN_ROUTE = "login_route"



fun EntryProviderScope<NavKey>.loginEntry(navigator: Navigator) {
    entry<LoginNavKey> {
        LoginRoute(
            onRegister = navigator::navigateToRegister,
            onOpenResetPassword = navigator::navigateToRequestReset,
            navigateToFavoriteScreen = navigator::navigateToFavorites,
        )
    }
}


fun NavController.navigateToLoginScreen(navOptions: NavOptions? = null) {
    this.navigate(LOGIN_ROUTE, navOptions)
}

fun NavGraphBuilder.loginScreen(
    onOpenResetPassword: () -> Unit,
    onRegister: () -> Unit,
    navigateToFavoriteScreen: () -> Unit,
) {
    composable(route = LOGIN_ROUTE) {
        LoginRoute(
            onRegister = onRegister,
            onOpenResetPassword = onOpenResetPassword,
            navigateToFavoriteScreen = { navigateToFavoriteScreen() },
        )
    }
}

@Composable
fun LoginRoute(
    onOpenResetPassword: () -> Unit,
    onRegister: () -> Unit,
    navigateToFavoriteScreen: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val authSuccess by viewModel.authSuccess.collectAsStateWithLifecycle()
    val googleSignIn =
        rememberLauncherForActivityResult(GoogleSignInContract()) { task ->
            viewModel.doGoogleLogin(task)
        }
    LoginScreen(viewModel, onOpenResetPassword = onOpenResetPassword, onRegister = onRegister) {
        googleSignIn.launch(viewModel.googleSignInIntent)
    }
    if (authSuccess) {
        navigateToFavoriteScreen()
    }
    ScreenCounter.increment()
}
