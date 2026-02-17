package com.francotte.search.result_mode

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.feature.search.api.SearchMode
import com.francotte.feature.search.api.SearchModeNavKey
import com.francotte.feature.search.api.navigateToSearchRecipes
import com.francotte.navigation.Navigator
import com.francotte.ui.LocalSnackbarHostState


fun EntryProviderScope<NavKey>.searchModeEntry(navigator: Navigator) {
    entry<SearchModeNavKey> {key ->
        SearchModeRoute(viewModel =
            androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<SearchModeViewModel, SearchModeViewModel.Factory>(
                key = key.searchMode.title,
            ) { factory ->
                factory.create(key.searchMode)
            },onItemSelected = navigator::navigateToSearchRecipes, onBack = navigator::goBack)
    }
}


@Composable
fun SearchModeRoute(
    viewModel: SearchModeViewModel = hiltViewModel(),
    onItemSelected: (String, SearchMode) -> Unit,
    onBack: () -> Unit,
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val snackBarHost = LocalSnackbarHostState.current
    val isReloading by viewModel.isReloading.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
            viewModel.snackBarMessage.collect {
                snackBarHost.showSnackbar(it)
        }
    }
    ItemSelectionGrid(searchMode = viewModel.searchMode, items = items, isRefreshing = isReloading, onRefresh = viewModel::onRefresh, onItemSelected = onItemSelected, onBack = onBack)
}
