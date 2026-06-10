package com.francotte.search.result_recipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.navigateToDetail
import com.francotte.feature.search.api.SearchRecipesNavKey
import com.francotte.model.LikeableRecipe
import com.francotte.navigation.Navigator


fun EntryProviderScope<NavKey>.searchRecipesEntry(
    navigator: Navigator,
    onToggleFavorite: (LikeableRecipe) -> Unit,
) {
    entry<SearchRecipesNavKey> { key ->
        SearchRecipesRoute(
            viewModel = hiltViewModel<SearchRecipesViewModel, SearchRecipesViewModel.Factory>(
                key = key.mode.title,
            ) { factory ->
                factory.create(key.item, key.mode)
            },
            onOpenRecipe = navigator::navigateToDetail,
            onToggleFavorite = onToggleFavorite,
            onBack = navigator::goBack,
        )
    }
}


@Composable
fun SearchRecipesRoute(
    viewModel: SearchRecipesViewModel = hiltViewModel(),
    onOpenRecipe: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SearchRecipesEvent.NavigateToRecipe -> onOpenRecipe(event.ids, event.index, event.title)
                SearchRecipesEvent.NavigateBack -> onBack()
            }
        }
    }

    SearchRecipesScreen(
        state = state,
        onAction = { action ->
            when (action) {
                // Favorite toggling stays outside the VM (FavoriteManager decoupling).
                is SearchRecipesAction.OnToggleFavorite -> onToggleFavorite(action.recipe)
                else -> viewModel.onAction(action)
            }
        },
    )
}
