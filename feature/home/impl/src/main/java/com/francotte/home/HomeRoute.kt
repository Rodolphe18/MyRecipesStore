package com.francotte.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.navigateToDetail
import com.francotte.common.counters.ScreenCounter
import com.francotte.feature.home.api.HomeNavKey
import com.francotte.feature.section.api.navigateToSection
import com.francotte.feature.video.api.navigateToVideo
import com.francotte.model.LikeableRecipe
import com.francotte.navigation.Navigator
import com.francotte.ui.LocalLaunchCounterManager
import com.francotte.ui.LocalSnackbarHostState


fun EntryProviderScope<NavKey>.homeEntry(navigator: Navigator,onToggleFavorite: (LikeableRecipe) -> Unit,
                                         ) {
    entry<HomeNavKey> {
        HomeRoute(
            onRecipeClick = navigator::navigateToDetail,
            onToggleFavorite = onToggleFavorite,
            onOpenSection = navigator::navigateToSection,
            onVideoButtonClick = navigator::navigateToVideo
        )
    }
}



@Composable
fun HomeRoute(
    homeViewModel: HomeViewModel = hiltViewModel(),
    onRecipeClick: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe) -> Unit,
    onOpenSection: (String) -> Unit,
    onVideoButtonClick: (String) -> Unit
) {
    val localLaunchCounter = LocalLaunchCounterManager.current
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val snackBarHostState = LocalSnackbarHostState.current
    LaunchedEffect(homeViewModel) {
        homeViewModel.snackBarEvent.collect { message ->
            snackBarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        HomeScreen(
            uiState= uiState,
            onOpenRecipe = onRecipeClick,
            onOpenSection = onOpenSection,
            onToggleFavorite = onToggleFavorite,
            onVideoButtonClick = onVideoButtonClick,
            onRefreshAll = homeViewModel::refreshAll,
            onRetryLatest = homeViewModel::retryLatestRecipes,
            onRetryJapanese = homeViewModel::retryJapaneseRecipes,
            onRetryAreas = homeViewModel::retryAreasRecipes,
            onRetryEnglish = homeViewModel::retryEnglishRecipes,
            onCurrentPageChange = homeViewModel::setLatestRecipesCurrentPage,
        )
    }
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
