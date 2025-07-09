package com.francotte.myrecipesstore.ui.compose.favorites.login

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.francotte.myrecipesstore.manager.FavoriteManager
import com.francotte.myrecipesstore.util.ScreenCounter
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

const val LOGIN_ROUTE = "login_route"

fun NavController.navigateToLoginScreen(navOptions: NavOptions? = null) {
    this.navigate(LOGIN_ROUTE, navOptions)
}


fun NavGraphBuilder.loginScreen(onRegister:() -> Unit, navigateToFavoriteScreen: () ->Unit, registerScreenDestination: NavGraphBuilder.() -> Unit,favoriteScreenDestination: NavGraphBuilder.() -> Unit) {

    composable(route = LOGIN_ROUTE) {
        LoginRoute(onRegister = onRegister, { navigateToFavoriteScreen() })
    }
    favoriteScreenDestination()
    registerScreenDestination()
}

@Composable
fun LoginRoute(onRegister:() -> Unit, navigateToFavoriteScreen: () ->Unit, viewModel: LoginViewModel= hiltViewModel()) {
    val authSuccess by viewModel.authSuccess.collectAsStateWithLifecycle()
    val googleSignIn = rememberLauncherForActivityResult(GoogleSignInContract()) { task ->
        viewModel.doGoogleLogin(task)
    }
    LoginScreen(viewModel, onRegister) {
        googleSignIn.launch(viewModel.googleSignInIntent)
    }
    if (authSuccess) {
        navigateToFavoriteScreen() }
    ScreenCounter.increment()
}

