package com.francotte.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.navigateToDetail
import com.francotte.common.counters.ScreenCounter
import com.francotte.feature.home.api.HomeNavKey
import com.francotte.feature.login.api.navigateToLogin
import com.francotte.feature.section.api.navigateToSection
import com.francotte.feature.video.api.navigateToVideo
import com.francotte.navigation.Navigator
import com.francotte.ui.LocalSnackbarHostState


fun EntryProviderScope<NavKey>.homeEntry(
    navigator: Navigator,
) {
    entry<HomeNavKey> {
        HomeRoute(
            onRecipeClick = navigator::navigateToDetail,
            onOpenSection = navigator::navigateToSection,
            onVideoButtonClick = navigator::navigateToVideo,
            onNavigateToLogin = navigator::navigateToLogin,
        )
    }
}


@Composable
fun HomeRoute(
    homeViewModel: HomeViewModel = hiltViewModel(),
    onRecipeClick: (List<String>, Int, String) -> Unit,
    onOpenSection: (String) -> Unit,
    onVideoButtonClick: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val state by homeViewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = LocalSnackbarHostState.current

    LaunchedEffect(homeViewModel) {
        homeViewModel.events.collect { event ->
            when (event) {
                is HomeEvent.NavigateToRecipe -> onRecipeClick(event.ids, event.index, event.title)
                is HomeEvent.NavigateToVideo -> onVideoButtonClick(event.youtubeUrl)
                is HomeEvent.NavigateToSection -> onOpenSection(event.sectionName)
                is HomeEvent.ShowSnackbar -> snackBarHostState.showSnackbar(
                    message = event.message,
                    duration = SnackbarDuration.Short,
                )
                HomeEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        HomeScreen(
            state = state,
            onAction = homeViewModel::onAction,
        )
    }
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
