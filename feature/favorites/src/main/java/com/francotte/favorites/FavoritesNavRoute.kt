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
import com.francotte.common.counters.ScreenCounter
import com.francotte.model.LikeableRecipe

const val FAVORITE_ROUTE = "favorite_route"

fun NavController.navigateToFavoriteScreen(navOptions: NavOptions? = null) {
    this.navigate(FAVORITE_ROUTE, navOptions)
}

fun NavGraphBuilder.favoritesScreen(
    windowSizeClass: WindowSizeClass,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    onOpenRecipe: (List<String>, Int, String) -> Unit,
    onCustomRecipeClick: (String) -> Unit,
    customRecipeHasBeenUpdated: Boolean
) {
    composable(route = FAVORITE_ROUTE,deepLinks = listOf(navDeepLink { uriPattern = "myapp://favorites" })) {
        FavoriteRoute(
            windowSizeClass = windowSizeClass,
            onRecipeClick = { ids, index, title -> onOpenRecipe(ids, index, title) },
            onToggleFavorite = onToggleFavorite,
            onCustomRecipeClick = { id -> onCustomRecipeClick(id) },
            customRecipeHasBeenUpdated = customRecipeHasBeenUpdated
        )
    }
}

@Composable
fun FavoriteRoute(
    viewModel: FavViewModel = hiltViewModel(),
    windowSizeClass: WindowSizeClass,
    onRecipeClick: (List<String>, Int, String) -> Unit,
    onCustomRecipeClick: (String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit,
    customRecipeHasBeenUpdated: Boolean
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
        customRecipeHasBeenUpdated = customRecipeHasBeenUpdated
    )
    ScreenCounter.increment()
}


