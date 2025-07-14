package com.francotte.myrecipesstore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.francotte.myrecipesstore.manager.AuthManager
import com.francotte.myrecipesstore.manager.FavoriteManager
import com.francotte.myrecipesstore.ui.compose.add_recipe.ADD_ROUTE
import com.francotte.myrecipesstore.ui.compose.add_recipe.navigateToAddRecipeScreen
import com.francotte.myrecipesstore.ui.compose.categories.CATEGORIES_ROUTE
import com.francotte.myrecipesstore.ui.compose.categories.navigateToCategoriesScreen
import com.francotte.myrecipesstore.ui.compose.favorites.FAVORITE_ROUTE
import com.francotte.myrecipesstore.ui.compose.login.LOGIN_ROUTE
import com.francotte.myrecipesstore.ui.compose.login.navigateToLoginScreen
import com.francotte.myrecipesstore.ui.compose.favorites.navigateToFavoriteScreen
import com.francotte.myrecipesstore.ui.compose.home.HOME_ROUTE
import com.francotte.myrecipesstore.ui.compose.home.navigateToHomeScreen
import com.francotte.myrecipesstore.ui.compose.search.SEARCH_ROUTE
import com.francotte.myrecipesstore.ui.compose.search.navigateToSearchScreen
import com.francotte.myrecipesstore.ui.navigation.TopLevelDestination
import com.francotte.myrecipesstore.util.LaunchCounterManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun rememberAppState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
    favoriteManager: FavoriteManager,
    authManager: AuthManager,
    launchCounterManager: LaunchCounterManager,
    resetPasswordToken:String?=null
): AppState {
    return remember(navController, coroutineScope) {
        AppState(navController, coroutineScope, favoriteManager, authManager, launchCounterManager,resetPasswordToken)
    }
}

@Stable
class AppState(
    val navController: NavHostController,
    coroutineScope: CoroutineScope,
    val favoriteManager: FavoriteManager,
    val authManager: AuthManager,
    launchCounterManager: LaunchCounterManager,
    val resetPasswordToken:String?=null
) {

    private val previousDestination = mutableStateOf<NavDestination?>(null)

    val currentDestination: NavDestination?
        @Composable get() {
            val currentEntry =
                navController.currentBackStackEntryFlow.collectAsState(initial = null)
            return currentEntry.value?.destination.also { destination ->
                if (destination != null) {
                    previousDestination.value = destination
                }
            } ?: previousDestination.value
        }

    val topLevelDestinations: List<TopLevelDestination>
        @Composable get() {
            return listOf(
                TopLevelDestination.HOME,
                TopLevelDestination.CATEGORIES,
                TopLevelDestination.ADD,
                TopLevelDestination.SEARCH,
                TopLevelDestination.FAVORITES,
                TopLevelDestination.LOGIN
            )
        }


    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            val destination = currentDestination ?: return null
            val currentRoute = destination.route

            return when (currentRoute) {
                HOME_ROUTE -> TopLevelDestination.HOME
                CATEGORIES_ROUTE -> TopLevelDestination.CATEGORIES
                SEARCH_ROUTE -> TopLevelDestination.SEARCH
                ADD_ROUTE -> TopLevelDestination.ADD
                FAVORITE_ROUTE -> TopLevelDestination.FAVORITES
                LOGIN_ROUTE -> TopLevelDestination.LOGIN
                else -> null
            }
        }


    init {
        coroutineScope.launch {
            favoriteManager.goToLoginScreenEvent.collect {
                navController.navigateToLoginScreen()
            }
        }
        launchCounterManager.incrementLaunchCount()
    }

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        val topLevelNavOptions = navOptions {
            popUpTo(navController.graph.findStartDestination().id)
        }

        when (topLevelDestination) {
            is TopLevelDestination.HOME -> navController.navigateToHomeScreen(topLevelNavOptions)
            is TopLevelDestination.CATEGORIES -> navController.navigateToCategoriesScreen(topLevelNavOptions)
            is TopLevelDestination.ADD -> navController.navigateToAddRecipeScreen(topLevelNavOptions)
            is TopLevelDestination.SEARCH -> navController.navigateToSearchScreen(topLevelNavOptions)
            is TopLevelDestination.FAVORITES -> navController.navigateToFavoriteScreen(topLevelNavOptions)
            is TopLevelDestination.LOGIN -> navController.navigateToLoginScreen(topLevelNavOptions)
        }
    }


}