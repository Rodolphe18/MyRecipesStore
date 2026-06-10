package com.francotte.add_recipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.AddRecipeNavKey
import com.francotte.common.counters.ScreenCounter
import com.francotte.feature.login.api.navigateToLogin
import com.francotte.navigation.Navigator
import com.francotte.ui.LocalSnackbarHostState

fun EntryProviderScope<NavKey>.addRecipeEntry(navigator: Navigator) {
    entry<AddRecipeNavKey> {
        AddRoute(navigator::navigateToLogin)
    }
}

@Composable
fun AddRoute(
    goToLoginScreen: () -> Unit,
    viewModel: AddRecipeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AddRecipeEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    AddRecipeScreen(
        state = state,
        onAction = { action ->
            when (action) {
                AddRecipeAction.OnGoToLogin -> goToLoginScreen()
                else -> viewModel.onAction(action)
            }
        },
    )
    LaunchedEffect(Unit) { ScreenCounter.increment() }
}
