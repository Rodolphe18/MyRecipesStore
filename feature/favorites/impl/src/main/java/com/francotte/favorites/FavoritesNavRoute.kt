package com.francotte.favorites

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.FavoritesNavKey
import com.francotte.api.navigateToCustomRecipe
import com.francotte.api.navigateToDetail
import com.francotte.common.counters.ScreenCounter
import com.francotte.model.LikeableRecipe
import com.francotte.navigation.Navigator
import kotlinx.serialization.Serializable

fun EntryProviderScope<NavKey>.favoritesEntry(
    navigator: Navigator,
    windowSizeClass: WindowSizeClass,
    onToggleFavorite: (LikeableRecipe) -> Unit,
    customRecipeHasBeenUpdated: Boolean
) {
    entry<FavoritesNavKey> {
        FavoriteRoute(
            windowSizeClass = windowSizeClass,
            onRecipeClick = navigator::navigateToDetail,
            onToggleFavorite = onToggleFavorite,
            onCustomRecipeClick = navigator::navigateToCustomRecipe,
            customRecipeHasBeenUpdated = customRecipeHasBeenUpdated,
        )
    }
}


@Composable
fun FavoriteRoute(
    viewModel: FavViewModel = hiltViewModel(),
    windowSizeClass: WindowSizeClass,
    onRecipeClick: (List<String>, Int, String) -> Unit,
    onCustomRecipeClick: (String) -> Unit,
    onToggleFavorite: (LikeableRecipe) -> Unit,
    customRecipeHasBeenUpdated: Boolean,
) {
    val favoriteUiState by viewModel.favoritesRecipesState.collectAsStateWithLifecycle()
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onSearchTextChange("")
    }
    FavoritesScreen(
        favoriteUiState = favoriteUiState,
        windowSizeClass = windowSizeClass,
        searchText = searchText,
        onSearchTextChanged = viewModel::onSearchTextChange,
        onOpenRecipe = onRecipeClick,
        onToggleFavorite = onToggleFavorite,
        onOpenCustomRecipe = onCustomRecipeClick,
        customRecipeHasBeenUpdated = customRecipeHasBeenUpdated,
    )
    ScreenCounter.increment()
}
