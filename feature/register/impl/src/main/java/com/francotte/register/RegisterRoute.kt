package com.francotte.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.RegisterNavKey
import com.francotte.api.navigateToFavorites
import com.francotte.common.counters.ScreenCounter
import com.francotte.navigation.Navigator
import com.francotte.ui.LocalSnackbarHostState


fun EntryProviderScope<NavKey>.registerEntry(navigator: Navigator) {
    entry<RegisterNavKey> {
        RegisterRoute(
            onBackPressed = navigator::goBack,
            navigateToFavoriteScreen = navigator::navigateToFavorites,
        )
    }
}

@Composable
fun RegisterRoute(
    onBackPressed: () -> Unit,
    navigateToFavoriteScreen: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RegisterEvent.NavigateToFavorites -> navigateToFavoriteScreen()
                is RegisterEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    RegisterScreen(
        state = state,
        onAction = { action ->
            when (action) {
                RegisterAction.OnBackClick -> onBackPressed()
                else -> viewModel.onAction(action)
            }
        },
    )
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
