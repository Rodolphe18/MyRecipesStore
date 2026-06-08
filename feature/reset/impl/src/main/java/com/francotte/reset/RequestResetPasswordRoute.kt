package com.francotte.reset

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.RequestResetNavKey
import com.francotte.common.counters.ScreenCounter
import com.francotte.login.LoginViewModel
import com.francotte.navigation.Navigator



fun EntryProviderScope<NavKey>.requestResetEntry(navigator: Navigator) {
    entry<RequestResetNavKey> {
        RequestResetPasswordRoute(navigator::goBack)
    }
}


@Composable
fun RequestResetPasswordRoute(
    onBackPressed: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    RequestResetPasswordScreen(viewModel, onBackPressed)
    ScreenCounter.increment()
}
