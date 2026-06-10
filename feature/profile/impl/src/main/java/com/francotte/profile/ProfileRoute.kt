package com.francotte.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.ProfileNavKey
import com.francotte.common.counters.ScreenCounter
import com.francotte.navigation.Navigator


fun EntryProviderScope<NavKey>.profileEntry(navigator: Navigator) {
    entry<ProfileNavKey> {
        ProfileRoute(onBackPressed = navigator::goBack)
    }
}


@Composable
fun ProfileRoute(
    onBackPressed: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileScreen(
        state = state,
        onAction = { action ->
            when (action) {
                ProfileAction.OnBackClick -> onBackPressed()
                else -> viewModel.onAction(action)
            }
        },
    )
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
