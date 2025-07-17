package com.francotte.myrecipesstore.ui.compose.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.francotte.myrecipesstore.MainActivityViewModel
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.ui.compose.composables.CustomTooltip
import com.francotte.myrecipesstore.ui.compose.launch_counter.LaunchCounterViewModel
import com.francotte.myrecipesstore.util.ScreenCounter

const val HOME_ROUTE = "home"
const val BASE_ROUTE = "base"

fun NavController.navigateToHomeScreen(navOptions: NavOptions? = null) {
    this.navigate(HOME_ROUTE, navOptions)
}

fun NavGraphBuilder.homeScreen(
    onRecipeClick: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onOpenSection: (String) -> Unit,
    onVideoButtonClick: (String) -> Unit,
    windowSizeClass: WindowSizeClass
) {
    navigation(startDestination = HOME_ROUTE, route = BASE_ROUTE) {
        composable(route = HOME_ROUTE) {
            HomeRoute(
                onRecipeClick = { ids, index, title -> onRecipeClick(ids, index, title) },
                onToggleFavorite = onToggleFavorite,
                onOpenSection = { onOpenSection(it) },
                onVideoButtonClick = onVideoButtonClick,
                windowSizeClass = windowSizeClass
            )
        }

    }
}

@Composable
fun HomeRoute(
    homeViewModel: HomeViewModel = hiltViewModel(),
    mainActivityViewModel: MainActivityViewModel = hiltViewModel(),
    launchCounterViewModel: LaunchCounterViewModel = hiltViewModel(),
    onRecipeClick: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onOpenSection: (String) -> Unit,
    onVideoButtonClick: (String) -> Unit,
    windowSizeClass: WindowSizeClass
) {
    val latestRecipes by homeViewModel.latestRecipes.collectAsStateWithLifecycle()
    val americanRecipes by homeViewModel.americanRecipes.collectAsStateWithLifecycle()
    val areasRecipes by homeViewModel.areasRecipes.collectAsStateWithLifecycle()
    val englishRecipes by homeViewModel.englishRecipes.collectAsStateWithLifecycle()
    val mainUiState by mainActivityViewModel.uiState.collectAsStateWithLifecycle()
    val launchCount by launchCounterViewModel.launchCount.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize()) {
        HomeScreen(
            latestRecipes = latestRecipes,
            americanRecipes = americanRecipes,
            areasRecipes = areasRecipes,
            englishRecipes = englishRecipes,
            onOpenRecipe = onRecipeClick,
            onOpenSection = onOpenSection,
            onToggleFavorite = onToggleFavorite,
            onVideoButtonClick = onVideoButtonClick,
            windowSizeClass = windowSizeClass
        )
        if (mainUiState.shouldShowToolTipEffect() && ScreenCounter.screenCount.get() < 2 && launchCount < 3) {
            CustomTooltip(
                modifier = Modifier.align(Alignment.BottomCenter),
                fullText = "Create your own recipe by clicking the button below"
            )
        }
    }
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }

}
