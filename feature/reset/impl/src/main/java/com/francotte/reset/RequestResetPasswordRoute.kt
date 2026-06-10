package com.francotte.reset

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.RequestResetNavKey
import com.francotte.common.counters.ScreenCounter
import com.francotte.navigation.Navigator


fun EntryProviderScope<NavKey>.requestResetEntry(navigator: Navigator) {
    entry<RequestResetNavKey> {
        RequestResetPasswordRoute(onBackPressed = navigator::goBack)
    }
}


@Composable
fun RequestResetPasswordRoute(
    onBackPressed: () -> Unit,
    viewModel: RequestResetPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RequestResetPasswordScreen(
        state = state,
        onAction = { action ->
            when (action) {
                RequestResetAction.OnBackClick -> onBackPressed()
                else -> viewModel.onAction(action)
            }
        },
    )
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
