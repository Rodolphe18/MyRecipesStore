package com.francotte.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.FavoritesNavKey
import com.francotte.api.navigateToCustomRecipe
import com.francotte.api.navigateToDetail
import com.francotte.common.counters.ScreenCounter
import com.francotte.model.LikeableRecipe
import com.francotte.navigation.Navigator

fun EntryProviderScope<NavKey>.favoritesEntry(
    navigator: Navigator,
    onToggleFavorite: (LikeableRecipe) -> Unit,
) {
    entry<FavoritesNavKey> {
        FavoriteRoute(
            onRecipeClick = navigator::navigateToDetail,
            onToggleFavorite = onToggleFavorite,
            onCustomRecipeClick = navigator::navigateToCustomRecipe,
        )
    }
}


@Composable
fun FavoriteRoute(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onRecipeClick: (List<String>, Int, String) -> Unit,
    onCustomRecipeClick: (String) -> Unit,
    onToggleFavorite: (LikeableRecipe) -> Unit,
) {
    val favoriteUiState by viewModel.favoritesRecipesState.collectAsStateWithLifecycle()
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onSearchTextChange("")
    }
    FavoritesScreen(
        favoriteUiState = favoriteUiState,
        searchText = searchText,
        onSearchTextChanged = viewModel::onSearchTextChange,
        onOpenRecipe = onRecipeClick,
        onToggleFavorite = onToggleFavorite,
        onOpenCustomRecipe = onCustomRecipeClick,
    )
    ScreenCounter.increment()
}
