package com.francotte.home

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
import com.francotte.common.counters.ScreenCounter
import com.francotte.designsystem.component.CustomTooltip
import com.francotte.model.LikeableRecipe
import com.francotte.ui.LocalLaunchCounterManager

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

@Composable
fun HomeRoute(
    homeViewModel: HomeViewModel = hiltViewModel(),
    onRecipeClick: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onOpenSection: (String) -> Unit,
    onVideoButtonClick: (String) -> Unit,
    windowSizeClass: WindowSizeClass
) {

    val localLaunchCounter = LocalLaunchCounterManager.current
    val latestRecipes by homeViewModel.latestRecipes.collectAsStateWithLifecycle()
    val americanRecipes by homeViewModel.americanRecipes.collectAsStateWithLifecycle()
    val areasRecipes by homeViewModel.areasRecipes.collectAsStateWithLifecycle()
    val englishRecipes by homeViewModel.englishRecipes.collectAsStateWithLifecycle()
    val isReloading by homeViewModel.isReloading.collectAsStateWithLifecycle()
    val currentPage by homeViewModel.currentPage.collectAsStateWithLifecycle()

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
            windowSizeClass = windowSizeClass,
            isReloading = isReloading,
            onReload = { homeViewModel.onPullToRefresh() },
            currentPage = currentPage,
            onCurrentPageChange = homeViewModel::setCurrentPage
        )
    }
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }

}
