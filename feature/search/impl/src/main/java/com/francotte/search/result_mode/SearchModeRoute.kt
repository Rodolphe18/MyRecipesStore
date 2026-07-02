package com.francotte.search.result_mode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.feature.search.api.SearchMode
import com.francotte.feature.search.api.SearchModeNavKey
import com.francotte.feature.search.api.navigateToSearchRecipes
import com.francotte.navigation.Navigator
import com.francotte.ui.LocalSnackbarHostState


fun EntryProviderScope<NavKey>.searchModeEntry(navigator: Navigator) {
    entry<SearchModeNavKey> { key ->
        SearchModeRoute(
            viewModel = hiltViewModel<SearchModeViewModel, SearchModeViewModel.Factory>(
                key = key.searchMode.title,
            ) { factory ->
                factory.create(key.searchMode)
            },
            onItemSelected = navigator::navigateToSearchRecipes,
            onBack = navigator::goBack,
        )
    }
}


@Composable
fun SearchModeRoute(
    viewModel: SearchModeViewModel = hiltViewModel(),
    onItemSelected: (String, SearchMode) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHost = LocalSnackbarHostState.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SearchModeEvent.NavigateToRecipes -> onItemSelected(event.item, event.mode)
                SearchModeEvent.NavigateBack -> onBack()
                is SearchModeEvent.ShowSnackbar -> snackBarHost.showSnackbar(event.message)
            }
        }
    }

    ItemSelectionGrid(state = state, onAction = viewModel::onAction)
}
