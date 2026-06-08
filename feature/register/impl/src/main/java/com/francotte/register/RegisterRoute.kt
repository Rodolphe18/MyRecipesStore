package com.francotte.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.RegisterNavKey
import com.francotte.api.navigateToFavorites
import com.francotte.common.counters.ScreenCounter
import com.francotte.navigation.Navigator


fun EntryProviderScope<NavKey>.registerEntry(navigator: Navigator) {
    entry<RegisterNavKey> {
        RegisterRoute(
            onBackPressed = navigator::goBack,
            navigateToFavoriteScreen = navigator::navigateToFavorites
        )
    }
}

@Composable
fun RegisterRoute(
    onBackPressed: () -> Unit,
    navigateToFavoriteScreen: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    RegisterScreen(onBackPressed, viewModel)
    LaunchedEffect(Unit) {
        viewModel.authSuccess.collect {
            navigateToFavoriteScreen()
        }
    }
    ScreenCounter.increment()
}
