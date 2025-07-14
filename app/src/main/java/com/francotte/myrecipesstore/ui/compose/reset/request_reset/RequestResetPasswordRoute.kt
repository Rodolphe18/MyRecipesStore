package com.francotte.myrecipesstore.ui.compose.reset.request_reset

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.ui.compose.login.LoginViewModel
import com.francotte.myrecipesstore.util.ScreenCounter

const val REQUEST_RESET_ROUTE = "request_reset_route"

fun NavController.navigateToRequestResetPasswordScreen(navOptions: NavOptions? = null) {
    this.navigate(REQUEST_RESET_ROUTE , navOptions)
}

fun NavGraphBuilder.requestResetPasswordScreen(onBackPressed: () -> Unit) {
    composable(route = REQUEST_RESET_ROUTE ) {
        RequestResetPasswordRoute(onBackPressed = onBackPressed)
    }

}

@Composable
fun RequestResetPasswordRoute(onBackPressed:()->Unit, viewModel: LoginViewModel = hiltViewModel()) {
    RequestResetPasswordScreen(viewModel, onBackPressed)
    ScreenCounter.increment()
}