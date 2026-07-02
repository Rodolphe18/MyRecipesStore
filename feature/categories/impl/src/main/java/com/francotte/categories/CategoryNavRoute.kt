package com.francotte.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.CategoryNavKey
import com.francotte.api.navigateToDetail
import com.francotte.common.counters.ScreenCounter
import com.francotte.model.LikeableRecipe
import com.francotte.navigation.Navigator
import com.francotte.ui.LocalSnackbarHostState


fun EntryProviderScope<NavKey>.categoryEntry(
    navigator: Navigator,
    onToggleFavorite: (LikeableRecipe) -> Unit,
) {
    entry<CategoryNavKey> { key ->
        CategoryRoute(
            viewModel = hiltViewModel<CategoryViewModel, CategoryViewModel.Factory>(key = key.category) { factory ->
                factory.create(key.category)
            },
            onOpenRecipe = navigator::navigateToDetail,
            onToggleFavorite = onToggleFavorite,
            onBack = navigator::goBack,
        )
    }
}


@Composable
fun CategoryRoute(
    viewModel: CategoryViewModel = hiltViewModel(),
    onOpenRecipe: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHost = LocalSnackbarHostState.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CategoryEvent.NavigateToRecipe -> onOpenRecipe(event.ids, event.index, event.title)
                CategoryEvent.NavigateBack -> onBack()
                is CategoryEvent.ShowSnackbar -> snackBarHost.showSnackbar(event.message)
            }
        }
    }
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }

    CategoryScreen(
        state = state,
        onAction = { action ->
            when (action) {
                // Favorite toggling stays outside the VM (FavoriteManager decoupling).
                is CategoryAction.OnToggleFavorite -> onToggleFavorite(action.recipe)
                else -> viewModel.onAction(action)
            }
        },
    )
}
