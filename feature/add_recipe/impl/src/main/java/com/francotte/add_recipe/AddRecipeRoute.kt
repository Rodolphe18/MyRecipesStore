package com.francotte.add_recipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.AddRecipeNavKey
import com.francotte.common.counters.ScreenCounter
import com.francotte.feature.login.api.navigateToLogin
import com.francotte.navigation.Navigator


fun EntryProviderScope<NavKey>.addRecipeEntry(navigator: Navigator,isAuthenticated:Boolean) {
    entry<AddRecipeNavKey> {
        AddRoute(isAuthenticated, navigator::navigateToLogin)
    }
}


@Composable
fun AddRoute(
    isAuthenticated: Boolean,
    goToLoginScreen: () -> Unit,
) {
    AddRecipeScreen(
        isAuthenticated = isAuthenticated,
        goToLoginScreen = { goToLoginScreen() },
    )
    LaunchedEffect(Unit) { ScreenCounter.increment() }
}
