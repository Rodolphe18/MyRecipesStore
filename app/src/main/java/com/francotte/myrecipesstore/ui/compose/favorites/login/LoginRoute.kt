package com.francotte.myrecipesstore.ui.compose.favorites.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object LoginNavigationRoute

fun NavController.navigateToLoginScreen(navOptions: NavOptions? = null) {
    this.navigate(LoginNavigationRoute, navOptions)
}

fun NavGraphBuilder.loginScreen(onBackPressed:() -> Unit, navigateToFavoriteScreen: () ->Unit,favoriteScreenDestination: NavGraphBuilder.() -> Unit) {

    composable<LoginNavigationRoute> {
        LoginRoute(onBackPressed = onBackPressed, { navigateToFavoriteScreen() })
    }
    favoriteScreenDestination()
}

@Composable
fun LoginRoute(onBackPressed:() -> Unit, navigateToFavoriteScreen: () ->Unit, viewModel: LoginViewModel= hiltViewModel()) {
    val authSuccess by viewModel.authSuccess.collectAsStateWithLifecycle()
   // val googleSignIn = activity.registerForActivityResult(GoogleSignInContract()) { task ->
    //    viewModel.doGoogleLogin(task)
   // }
    LoginScreen(viewModel, onBackPressed)
    if (authSuccess) navigateToFavoriteScreen()
}

