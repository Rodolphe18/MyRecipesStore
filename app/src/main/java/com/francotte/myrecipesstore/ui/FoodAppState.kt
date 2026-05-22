package com.francotte.myrecipesstore.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.francotte.api.DetailRecipeNavKey
import com.francotte.feature.home.api.HomeNavKey
import com.francotte.myrecipesstore.navigation.TOP_LEVEL_NAV_ITEMS
import com.francotte.myrecipesstore.splash.SplashNavKey
import com.francotte.navigation.NavigationState
import com.francotte.navigation.rememberNavigationState
import com.francotte.ui.TrackDisposableJank

@Composable
fun rememberAppState(resetPasswordToken: String? = null): AppState {
    val navigationState = rememberNavigationState(SplashNavKey,HomeNavKey, TOP_LEVEL_NAV_ITEMS.keys+ DetailRecipeNavKey())
    NavigationTrackingSideEffect(navigationState)

    return remember {
        AppState(navigationState, resetPasswordToken)
    }
}

@Stable
class AppState(
    val navigationState: NavigationState,
    val resetPasswordToken: String? = null,
)

@Composable
private fun NavigationTrackingSideEffect(navigationState: NavigationState) {
    TrackDisposableJank(navigationState.currentKey) { metricsHolder ->
        metricsHolder.state?.putState("Navigation", navigationState.currentKey.toString())
        onDispose {
            metricsHolder.state?.removeState("Navigation")
        }
    }
}
