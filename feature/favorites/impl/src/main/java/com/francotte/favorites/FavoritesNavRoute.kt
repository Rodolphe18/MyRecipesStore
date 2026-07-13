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
import com.francotte.feature.login.api.navigateToLogin
import com.francotte.navigation.Navigator
import com.francotte.ui.LocalSnackbarHostState

fun EntryProviderScope<NavKey>.favoritesEntry(
    navigator: Navigator,
) {
    entry<FavoritesNavKey> {
        FavoriteRoute(
            onRecipeClick = navigator::navigateToDetail,
            onCustomRecipeClick = navigator::navigateToCustomRecipe,
            onNavigateToLogin = navigator::navigateToLogin,
        )
    }
}


@Composable
fun FavoriteRoute(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onRecipeClick: (List<String>, Int, String) -> Unit,
    onCustomRecipeClick: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHost = LocalSnackbarHostState.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is FavoritesEvent.NavigateToRecipe -> onRecipeClick(event.ids, event.index, event.title)
                is FavoritesEvent.NavigateToCustomRecipe -> onCustomRecipeClick(event.recipeId)
                is FavoritesEvent.ShowSnackbar -> snackBarHost.showSnackbar(event.message)
                FavoritesEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.onAction(FavoritesAction.OnSearchChange(""))
    }

    FavoritesScreen(
        state = state,
        onAction = viewModel::onAction,
    )
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
