package com.francotte.myrecipesstore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.francotte.myrecipesstore.favorites.FavoriteManager
import com.francotte.myrecipesstore.ui.compose.categories.navigateToCategoriesScreen
import com.francotte.myrecipesstore.ui.compose.favorites.login.navigateToLoginScreen
import com.francotte.myrecipesstore.ui.compose.favorites.navigateToFavoriteScreen
import com.francotte.myrecipesstore.ui.compose.home.navigateToHomeScreen
import com.francotte.myrecipesstore.ui.navigation.TopLevelDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun rememberAppState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
    favoriteManager: FavoriteManager,
    isAuthenticated: Boolean
): AppState {
    return remember(navController, coroutineScope) {
        AppState(navController, coroutineScope, favoriteManager, isAuthenticated)
    }
}

@Stable
class AppState(
    val navController: NavHostController,
    coroutineScope: CoroutineScope,
    private val favoriteManager: FavoriteManager,
    private val isAuthenticated: Boolean
) {

    private val previousDestination = mutableStateOf<NavDestination?>(null)

    val currentDestination: NavDestination?
        @Composable get() {
            val currentEntry = navController.currentBackStackEntryFlow.collectAsState(initial = null)
            return currentEntry.value?.destination.also { destination ->
                if (destination != null) {
                    previousDestination.value = destination
                }
            } ?: previousDestination.value
        }

    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            return TopLevelDestination.entries.firstOrNull { topLevelDestination ->
                currentDestination?.hasRoute(route = topLevelDestination.route) == true
            }
        }



    init {
        coroutineScope.launch {
            favoriteManager.goToLoginScreenEvent.collectLatest {
                navController.navigateToLoginScreen()
            }
        }
    }

    fun navigateToTopLevelDestination(
        topLevelDestination: TopLevelDestination
    ) {
        val topLevelNavOptions = navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        when (topLevelDestination) {
            TopLevelDestination.HOME -> navController.navigateToHomeScreen(topLevelNavOptions)
            TopLevelDestination.CATEGORIES -> navController.navigateToCategoriesScreen(topLevelNavOptions)
            TopLevelDestination.FAVORITES -> {
               if (isAuthenticated) navController.navigateToFavoriteScreen(topLevelNavOptions) else
                    navController.navigateToLoginScreen(topLevelNavOptions)
            }
        }
    }


}