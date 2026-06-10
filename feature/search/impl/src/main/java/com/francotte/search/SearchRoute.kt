package com.francotte.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.navigateToDetail
import com.francotte.common.counters.ScreenCounter
import com.francotte.feature.search.api.SearchMode
import com.francotte.feature.search.api.SearchNavKey
import com.francotte.feature.search.api.navigateToSearchMode
import com.francotte.feature.search.api.navigateToSearchRecipes
import com.francotte.model.LikeableRecipe
import com.francotte.navigation.Navigator


fun EntryProviderScope<NavKey>.searchEntry(navigator: Navigator, onToggleFavorite: (LikeableRecipe) -> Unit) {
    entry<SearchNavKey> {
        SearchRoute(
            onSearchModeSelected = navigator::navigateToSearchMode,
            onSearchTypeClick = navigator::navigateToSearchRecipes,
            onOpenRecipe = navigator::navigateToDetail,
            onToggleFavorite = onToggleFavorite,
        )
    }
}


@Composable
fun SearchRoute(
    viewModel: SearchViewModel = hiltViewModel(),
    onSearchModeSelected: (SearchMode) -> Unit,
    onSearchTypeClick: (String, SearchMode) -> Unit,
    onOpenRecipe: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SearchEvent.NavigateToSearchMode -> onSearchModeSelected(event.mode)
                is SearchEvent.NavigateToSearchRecipes -> onSearchTypeClick(event.item, event.mode)
                is SearchEvent.NavigateToRecipe -> onOpenRecipe(event.ids, event.index, event.title)
            }
        }
    }

    SearchScreen(
        state = state,
        onAction = { action ->
            when (action) {
                // Favorite toggling stays outside the VM (FavoriteManager decoupling).
                is SearchAction.OnToggleFavorite -> onToggleFavorite(action.recipe)
                else -> viewModel.onAction(action)
            }
        },
    )
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
    LaunchedEffect(Unit) {
        viewModel.ensureFtsReady()
    }
}
