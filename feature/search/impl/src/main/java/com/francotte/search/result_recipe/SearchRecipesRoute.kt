package com.francotte.search.result_recipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.navigateToDetail
import com.francotte.feature.login.api.navigateToLogin
import com.francotte.feature.search.api.SearchRecipesNavKey
import com.francotte.navigation.Navigator
import com.francotte.ui.LocalSnackbarHostState


fun EntryProviderScope<NavKey>.searchRecipesEntry(
    navigator: Navigator,
) {
    entry<SearchRecipesNavKey> { key ->
        SearchRecipesRoute(
            viewModel = hiltViewModel<SearchRecipesViewModel, SearchRecipesViewModel.Factory>(
                key = key.mode.title,
            ) { factory ->
                factory.create(key.item, key.mode)
            },
            onOpenRecipe = navigator::navigateToDetail,
            onBack = navigator::goBack,
            onNavigateToLogin = navigator::navigateToLogin,
        )
    }
}


@Composable
fun SearchRecipesRoute(
    viewModel: SearchRecipesViewModel = hiltViewModel(),
    onOpenRecipe: (List<String>, Int, String) -> Unit,
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHost = LocalSnackbarHostState.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SearchRecipesEvent.NavigateToRecipe -> onOpenRecipe(event.ids, event.index, event.title)
                SearchRecipesEvent.NavigateBack -> onBack()
                is SearchRecipesEvent.ShowSnackbar -> snackBarHost.showSnackbar(event.message)
                SearchRecipesEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    SearchRecipesScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}
