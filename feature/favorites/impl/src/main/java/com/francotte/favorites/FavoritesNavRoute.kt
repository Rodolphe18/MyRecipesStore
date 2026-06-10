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
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is FavoritesEvent.NavigateToRecipe -> onRecipeClick(event.ids, event.index, event.title)
                is FavoritesEvent.NavigateToCustomRecipe -> onCustomRecipeClick(event.recipeId)
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.onAction(FavoritesAction.OnSearchChange(""))
    }

    FavoritesScreen(
        state = state,
        onAction = { action ->
            when (action) {
                // Favorite toggling stays outside the VM (FavoriteManager decoupling).
                is FavoritesAction.OnToggleFavorite -> onToggleFavorite(action.recipe)
                else -> viewModel.onAction(action)
            }
        },
    )
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
