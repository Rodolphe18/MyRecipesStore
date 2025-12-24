package com.francotte.myrecipesstore.navigation

import android.content.Context
import android.view.Window
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.francotte.home.BASE_ROUTE
import com.francotte.myrecipesstore.splash.SPLASH_ROUTE
import com.francotte.myrecipesstore.splash.SplashRoute
import com.francotte.myrecipesstore.splash.goToBaseAndClearSplash
import com.francotte.myrecipesstore.ui.AppState
import dagger.hilt.android.qualifiers.ApplicationContext

@Composable
fun RootNavHost(
    @ApplicationContext context: Context,
    rootNavController: NavHostController,
    appState: AppState,
    windowSizeClass: WindowSizeClass,
    window: Window
) {
    NavHost(
        navController = rootNavController,
        startDestination = SPLASH_ROUTE
    ) {
        composable(SPLASH_ROUTE) {
            SplashRoute(
                onDone = { rootNavController.goToBaseAndClearSplash() }
            )
        }
        composable(BASE_ROUTE) {
            BaseScaffold(
                context = context,
                appState = appState,
                windowSizeClass = windowSizeClass,
                window = window
            )
        }
    }
}