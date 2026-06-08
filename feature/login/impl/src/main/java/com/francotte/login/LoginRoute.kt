package com.francotte.login

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.francotte.ui.LocalSnackbarHostState
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
    val activity = LocalActivity.current
    val snackbarHostState = LocalSnackbarHostState.current
    LaunchedEffect(Unit) {
        launch { viewModel.authSuccess.collect { navigateToFavoriteScreen() } }
        launch {
            viewModel.authError.collect { message ->
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            }
        }
    }
    LoginScreen(
        onOpenResetPassword = onOpenResetPassword,
        onLogin = viewModel::loginWithMailAndPassword,
        onRegister = onRegister,
    ) {
        activity?.let { viewModel.doGoogleLogin(it) }
    }
    ScreenCounter.increment()
}
