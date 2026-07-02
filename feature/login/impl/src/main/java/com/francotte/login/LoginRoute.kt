package com.francotte.login

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.navigateToFavorites
import com.francotte.api.navigateToRegister
import com.francotte.api.navigateToRequestReset
import com.francotte.common.counters.ScreenCounter
import com.francotte.feature.login.api.LoginNavKey
import com.francotte.navigation.Navigator
import com.francotte.ui.LocalSnackbarHostState


fun EntryProviderScope<NavKey>.loginEntry(navigator: Navigator) {
    entry<LoginNavKey> {
        LoginRoute(
            onRegister = navigator::navigateToRegister,
            onOpenResetPassword = navigator::navigateToRequestReset,
            navigateToFavoriteScreen = navigator::navigateToFavorites,
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
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.NavigateToFavorites -> navigateToFavoriteScreen()
                is LoginEvent.ShowSnackbar -> snackbarHostState.showSnackbar(
                    message = event.message,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    LoginScreen(
        state = state,
        onAction = { action ->
            when (action) {
                // Google login needs an Activity; register/reset are pure navigation.
                LoginAction.OnGoogleLoginClick -> activity?.let { viewModel.doGoogleLogin(it) }
                LoginAction.OnRegisterClick -> onRegister()
                LoginAction.OnResetPasswordClick -> onOpenResetPassword()
                else -> viewModel.onAction(action)
            }
        },
    )
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
